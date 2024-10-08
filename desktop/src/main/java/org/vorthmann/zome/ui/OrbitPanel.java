package org.vorthmann.zome.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.vorthmann.ui.CardPanel;

import com.vzome.desktop.api.Controller;
import com.vzome.desktop.awt.GraphicsController;
import com.vzome.desktop.awt.OrbitSetGraphicsController;

@SuppressWarnings("serial")
public class OrbitPanel extends JPanel implements PropertyChangeListener
{
	@Override
	public void setToolTipText( String text )
	{
		orbitTriangle .setToolTipText( text );
		orbitCheckboxes .setToolTipText( text );
	}

	private GraphicsController enabledOrbits;
	private Controller drawnOrbits;
	private final ContextualMenu directionPopupMenu;
	private final JPanel orbitTriangle, orbitCheckboxes;
	private final CardPanel cardPanel;
	private MouseListener orbitPopup;
	private JCheckBox singleCheckbox;
    private final boolean showLastOrbit;
	
	public OrbitPanel( final Controller selectedOrbits, final Controller drawnOrbits, ControlActions enabler )
	{
		this .showLastOrbit = enabler != null;
        this .enabledOrbits = new OrbitSetGraphicsController( selectedOrbits, this .showLastOrbit );
		this .drawnOrbits = drawnOrbits;
		
		orbitTriangle = new JPanel()
        {
            @Override
            public void paintComponent( Graphics graphics )
            {
            	enabledOrbits .repaintGraphics( "orbits", graphics, getSize() );
            }
        };
        orbitCheckboxes = new JPanel();
        
        final ActionListener indirection = new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent evt )
            {
                enabledOrbits .actionPerformed( evt .getSource(), evt .getActionCommand() );
            }
        };

        // here's the containment hierarchy
        this .setLayout( new BorderLayout() );
        {
            JPanel controlStrip = new JPanel();
            controlStrip .setLayout( new BorderLayout() );
            {
                JPanel row1 = new JPanel();
                row1 .setLayout( new GridLayout( 0, 3 ) );
                row1 .add( createButton( "None", "setNoDirections", indirection ) );
                singleCheckbox = createCheckbox( "single", "oneAtATime", indirection );
                if ( "true" .equals( enabledOrbits .getProperty( "oneAtATime" ) ) )
                    singleCheckbox .setSelected( true );
                row1 .add( createButton( "All", "setAllDirections", new ActionListener()
                {
                    @Override
                    public void actionPerformed( ActionEvent evt )
                    {
                        indirection .actionPerformed( evt );
                        singleCheckbox .setSelected( false );
                    }
                } ) );
                row1 .add( singleCheckbox, BorderLayout.WEST );
                controlStrip .add( row1, BorderLayout.NORTH );
            }
            this .add( controlStrip, BorderLayout.NORTH );
        }
        {
            cardPanel = new CardPanel();
            cardPanel .add( "graphical", orbitTriangle );
            {
                orbitCheckboxes .setLayout( new BoxLayout( orbitCheckboxes, 1 ) );
                JScrollPane scrollPanel = new JScrollPane( orbitCheckboxes );
                cardPanel .add( "textual", scrollPanel );
            }
            this .add( cardPanel, BorderLayout.CENTER );
        }

        if ( enabler != null )
        {
            directionPopupMenu = new ContextualMenu();
            directionPopupMenu.setLightWeightPopupEnabled( false );

            directionPopupMenu.add( enabler .setMenuAction( "rZomeOrbits",         drawnOrbits, new JMenuItem( "real Zome" ) ) );
            directionPopupMenu.add( enabler .setMenuAction( "predefinedOrbits",    drawnOrbits, new JMenuItem( "predefined" ) ) );
            directionPopupMenu.add( enabler .setMenuAction( "usedOrbits",          drawnOrbits, new JMenuItem( "used in model" ) ) );
            directionPopupMenu.add( enabler .setMenuAction( "setAllDirections",    drawnOrbits, new JMenuItem( "all" ) ) );
            directionPopupMenu.add( enabler .setMenuAction( "configureDirections", drawnOrbits, new JMenuItem( "configure..." ) ) );
            directionPopupMenu.add( enabler .setMenuAction( "resetOrbitColors",    drawnOrbits, new JMenuItem( "reset colors" ) ) );
        }
        else
        	directionPopupMenu = null;
        
        modeChanged( enabledOrbits .propertyIsTrue( "useGraphicalViews" ) );
        systemChanged( this .enabledOrbits, this .drawnOrbits );
	}
    
	void modeChanged( boolean graphical )
	{
		if ( graphical )
            cardPanel .showCard( "graphical" );
		else
            cardPanel .showCard( "textual" );
	}

	public void systemChanged( Controller buildOrbits, Controller shownOrbits )
	{
        if ( orbitPopup != null )
        	orbitTriangle .removeMouseListener( orbitPopup );

        this .enabledOrbits .getMouseTool() .detach( orbitTriangle );
        this .drawnOrbits .removePropertyListener( this );
        this .enabledOrbits .removePropertyListener( this );

        this .drawnOrbits = shownOrbits;
        this .enabledOrbits = new OrbitSetGraphicsController( buildOrbits, this .showLastOrbit );
		
        this .singleCheckbox .setSelected( enabledOrbits .propertyIsTrue( "oneAtATime" ) );

        enabledOrbits .addPropertyListener( this );
        drawnOrbits .addPropertyListener( this );
        enabledOrbits .getMouseTool() .attach( orbitTriangle );

        if ( directionPopupMenu != null )
        {
            orbitPopup = new ContextualMenuMouseListener( enabledOrbits, directionPopupMenu );
            orbitTriangle .addMouseListener( orbitPopup );
            orbitCheckboxes .addMouseListener( orbitPopup );
        }

        orbitsChanged();
    }

	public void orbitsChanged()
	{
	    enabledOrbits .actionPerformed( orbitTriangle, "refreshDots" );

	    orbitCheckboxes .removeAll();
	    for ( String orbitName : enabledOrbits .getCommandList( "allOrbits" ) )
	    {
            JPanel panel = new JPanel();
            panel .setMaximumSize( new Dimension( 600, 20 ) );
            panel .setLayout( new BorderLayout() );
            final JPanel colorSwatch = new JPanel()
            {
                @Override
                public void paintComponent( Graphics graphics )
                {
                    enabledOrbits .repaintGraphics( "oneOrbit." + orbitName, graphics, getSize() );
                }
            };
            colorSwatch .setMaximumSize( new Dimension( 60, 20 ) );
            colorSwatch .setPreferredSize( new Dimension( 60, 20 ) );
            colorSwatch .setMinimumSize( new Dimension( 60, 20 ) );
            panel .add( colorSwatch, BorderLayout.WEST );
            {
                JCheckBox checkbox  = new JCheckBox();
                checkbox .setText( orbitName );
                checkbox .setVisible( true );
                checkbox .setSelected( false );
                checkbox .setActionCommand( "toggleDirection." + orbitName );
                checkbox .addActionListener( new ControllerActionListener(enabledOrbits) );
                panel .add( checkbox, BorderLayout.CENTER );
            }
            orbitCheckboxes .add( panel );
        }
	    enabledChanged();
	}
    
    private void enabledChanged()
    {
        this .singleCheckbox .setSelected( enabledOrbits .propertyIsTrue( "oneAtATime" ) );

        String[] dirNames = enabledOrbits .getCommandList( "orbits" );
        for ( int i = 0; i < orbitCheckboxes .getComponentCount(); i++ ) {
            JPanel row  = (JPanel) orbitCheckboxes .getComponent( i );
            JCheckBox checkbox  = (JCheckBox) row .getComponent( 1 );
            checkbox .setSelected( false );
        }
        for ( int j = 0; j < dirNames.length; j++ )
        {
            for ( int i = 0; i < orbitCheckboxes .getComponentCount(); i++ ) {
                JPanel row  = (JPanel) orbitCheckboxes .getComponent( i );
                JCheckBox checkbox  = (JCheckBox) row .getComponent( 1 );
                if ( dirNames[ j ] .equals( checkbox .getText() ) ) {
                    checkbox .setSelected( true );
                    break;
                }
            }
        }
        orbitTriangle .repaint();
        orbitCheckboxes .repaint(); 
        // The repaint leaves some instances blank!  don't know why.
        //  This seems to address the problem.
        orbitCheckboxes .setVisible( false );
        orbitCheckboxes .setVisible( true );
    }

	@Override
	public void propertyChange( PropertyChangeEvent event )
	{
	    switch ( event .getPropertyName() ) {

	    case "orbits":
	        orbitsChanged();
	        break;

	    case "useGraphicalViews":
	        if ( Boolean.TRUE .equals( event .getNewValue() ) )
	            cardPanel .showCard( "graphical" );
	        else
	            cardPanel .showCard( "textual" );
	        break;

	    default:
	        break;
	    }
	}

    protected static JButton createButton( String buttonText, String actionCommand, ActionListener listener )
    {
        JButton button = new JButton( buttonText );
        button .addActionListener( listener );
        button .setActionCommand( actionCommand );        
        return button;
    }

    protected static JCheckBox createCheckbox( String buttonText, String actionCommand, ActionListener listener )
    {
        JCheckBox button = new JCheckBox( buttonText, false );
        button .addActionListener( listener );
        button .setActionCommand( actionCommand );        
        return button;
    }
}
