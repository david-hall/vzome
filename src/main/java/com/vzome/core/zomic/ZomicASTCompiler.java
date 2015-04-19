package com.vzome.core.zomic;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.TokenStreamRecognitionException;
import com.vzome.core.algebra.AlgebraicNumber;
// Note that the com.vzome.core.antlr.generated classes can't be resolved after "clean"ing the project.
// They will be automatically re-generated by the build task and can then be resolved.
// To manually regenerate these classes without a complete build, run the gradle compileAntlrGrammars task.
import com.vzome.core.antlr.generated.ZomicLexer;
import com.vzome.core.antlr.generated.ZomicParser;
import com.vzome.core.antlr.generated.ZomicParserBaseListener;
import com.vzome.core.math.symmetry.Axis;
import com.vzome.core.math.symmetry.Direction;
import com.vzome.core.math.symmetry.IcosahedralSymmetry;
import com.vzome.core.render.ZomicEventHandler;
import com.vzome.core.zomic.parser.ErrorHandler;
import com.vzome.core.zomic.program.Build;
import com.vzome.core.zomic.program.Label;
import com.vzome.core.zomic.program.Move;
import com.vzome.core.zomic.program.Nested;
import com.vzome.core.zomic.program.Reflect;
import com.vzome.core.zomic.program.Repeat;
import com.vzome.core.zomic.program.Rotate;
import com.vzome.core.zomic.program.Save;
import com.vzome.core.zomic.program.Symmetry;
import com.vzome.core.zomic.program.Scale;
import com.vzome.core.zomic.program.Untranslatable;
import com.vzome.core.zomic.program.Walk;
import com.vzome.core.zomic.program.ZomicStatement;
import java.io.IOException;
import static java.lang.Math.abs;
import java.util.Stack;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Created by David Hall on 3/20/2015.
 */
public class ZomicASTCompiler
    extends ZomicParserBaseListener
{
	private final IcosahedralSymmetry icosaSymmetry;
	private final ZomicNamingConvention namingConvention ;
	private final Stack<ZomicStatement> statements = new Stack<>();
	private final Stack<ZomicStatementTemplate> templates = new Stack<>();
	private static boolean doPrint = true; // Only intended to be set from test methods

    public ZomicASTCompiler( IcosahedralSymmetry icosaSymm ) {
        icosaSymmetry = icosaSymm;
		namingConvention = new ZomicNamingConvention( icosaSymm );
    }

	public static Walk compileFile( String fileName, IcosahedralSymmetry symm, boolean showProgressMessages ) {
        doPrint = showProgressMessages;
		Walk program = null;
		ErrorHandler.Default errors = new ErrorHandler.Default();
		ZomicASTCompiler compiler = new ZomicASTCompiler(symm );
		try {
			ANTLRFileStream fileStream = new ANTLRFileStream(fileName);
			program = compiler.compile( fileStream, errors );
			if( program != null ) {
				program.setErrors( errors.getErrors() );
			}
		} catch (IOException ex) {
			errors.parseError( ErrorHandler.UNKNOWN, ErrorHandler.UNKNOWN, ex.getMessage() );
		}
        return program;
    }
	
	public static Walk compile( CharStream input, IcosahedralSymmetry symm, boolean showProgressMessages ) {
        doPrint = showProgressMessages;
		ErrorHandler.Default errors = new ErrorHandler.Default();
		ZomicASTCompiler compiler = new ZomicASTCompiler(symm );
        Walk program = compiler.compile( input, errors );
		if( program != null ) {
			program.setErrors( errors.getErrors() );
		}
        return program;
    }
	
	public static Walk compile( String input, IcosahedralSymmetry symm, boolean showProgressMessages ) {
		return compile( new ANTLRInputStream( input ), symm, showProgressMessages );
	}
	
	public static Walk compile( String input, IcosahedralSymmetry symm ) {
		return compile( input, symm, false );
	}
	
	public Walk compile( CharStream input, ErrorHandler errors ) {
        try  {
            return compile( input );
        } catch( RecognitionException ex ) {
            errors.parseError( ex.getLine(), ex.getColumn(), ex.getMessage() );
        } catch (TokenStreamRecognitionException ex ) {
            RecognitionException re = ex.recog;
            errors.parseError( re.getLine(), re .getColumn(), re.getMessage() );
        } catch (ParseCancellationException ex) {
			int line = ErrorHandler.UNKNOWN;
			int column = ErrorHandler.UNKNOWN;
			String msg = "Parser Cancelled.";
			Throwable cause = ex.getCause();
			if( cause instanceof InputMismatchException ) {
				InputMismatchException immEx = (InputMismatchException) cause;
				Token offender = immEx.getOffendingToken();
				if( offender != null ) {
					line = offender.getLine();
					column = offender.getCharPositionInLine();
					String txt = offender.getText();
					if(txt != null) {
						msg = " Unexpected Token '" + txt + "'.";
					}
				}
			}
            errors.parseError( line, column, msg );
        } catch (TokenStreamException ex) {
            errors.parseError( ErrorHandler.UNKNOWN, ErrorHandler.UNKNOWN, ex.getMessage() );
        }		
        return getProgram();
    }

	protected void reset() {
		tabs = 0; 
		statements.clear();
		templates.clear();
	}
	
	private static class RuntimeWrapperException 
	extends RuntimeException 
	{
		RuntimeWrapperException(LexerNoViableAltException ex) {
			this.initCause(ex);
		}
	}
	
	public static class StrictZomicLexer 
	extends ZomicLexer 
	{
		public StrictZomicLexer(CharStream input) { super(input); }
		public void recover(LexerNoViableAltException e) {
			// Bail out of the lexer at the first lexical error instead of trying to recover.
			// Use this in conjunction with BailErrorStrategy.
			// Wrap the LexerNoViableAltException in a RuntimeWrapperException
			// to be sure the lexer doesn't handle the LexerNoViableAltException.
			// The LexerNoViableAltException will be extracted from the RuntimeWrapperException
			// inside the compile() method below and re-thrown without the RuntimeWrapperException
			// since we'll be outside of the lexer rules at that point.
			throw new RuntimeWrapperException(e); 
		}
	}
	
	protected Walk compile( CharStream inputStream )
		throws RecognitionException, 
			TokenStreamException,
			ParseCancellationException // thrown by BailErrorStrategy
	{
		try {
			reset(); // in case a single instance compiles more than one program
			// feed input to lexer (either a ZomicLexer or, preferably a StrictZomicLexer)
			ZomicLexer lexer = new StrictZomicLexer( inputStream );
			// get a stream of matched tokens
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			// pass tokens to the parser
			ZomicParser parser = new ZomicParser( tokens );

			if(lexer instanceof StrictZomicLexer) {
			// Use this only in conjunction with the StrictZomicLexer class
			// bail out of the parser upon the first syntax error 
			// instead of using the default error strategy which tries to recover if possible.
				parser.setErrorHandler(new BailErrorStrategy());
			}

			// specify our entry point (top level rule)
			ZomicParser.ProgramContext program = parser.program(); // parse

			// Use the DEFAULT walker to walk from the entry point with this listener attached.
			// In the process, the enter and exit methods of this class will be invoked to populate the statements collection.
			ParseTreeWalker.DEFAULT.walk(this, program);
		}
		catch( RuntimeWrapperException ex ) {
			// unwrap the LexerNoViableAltException from the RuntimeWrapperException 
			// and rethrow it as described in StrictZomicLexer.recover()
			throw (LexerNoViableAltException) ex.getCause();
		}
		// Now we return the statement(s) collected by the listener.
		return getProgram();
	}
	
	protected Walk getProgram() {
		return statements.size() == 0 
				? new Walk() // just so we never return null, even parsing errors or an empty string
				: (Walk) statements.firstElement();
	}
	
	protected static int parseInt( Token token ) {
		return Integer.parseInt( token.getText() );
	}

	protected void prepareTemplate( ZomicStatementTemplate template ) {
		templates.push(template);
	}
	
	protected void prepareStatement( ZomicStatement statement ) {
		statements.push(statement);
	}
	
	protected void commitLastStatement() {
		ZomicStatement statement = statements.pop();
		if ( statement instanceof Nested ) {
			ZomicStatement body = ((Nested)statement).getBody();
			if ( (body == null) || ( (body instanceof Walk) && ((Walk)body).size() == 0 ) ) {
				// don't bother saving an empty Nested statement
				return;
			}
		}
		commit(statement);
	}
			
	protected void commit(ZomicStatement newStatement)	{
		ZomicStatement currentStatement = statements.peek();
		if ( currentStatement instanceof Walk ) {
			((Walk) currentStatement).addStatement( newStatement );
		} else {
			((Nested) currentStatement).setBody( newStatement );
		}
	}

	private int tabs = 0;
	private void showTabs(boolean add) 	{
		if(doPrint) { 
			if(!add) { tabs--; }
			for( int i = 0; i < tabs; i++)
			{
				print("   ");
			}
			if(add) { tabs++; }
		}
	}

	protected void printContext(ParserRuleContext ctx, boolean isEntering) 	{ 
		if(!doPrint) { return; }
		showTabs(isEntering);
		String contextName = ctx.getClass().getSimpleName();
		String strContext = "Context";
		if(contextName.endsWith(strContext)) {
			// strip "Context" from the name for readability
			contextName = contextName.substring(0, contextName.length() - strContext.length());
		}
		println((isEntering ? "--> " : "<-- ") + contextName);
	}

	protected static void print(String msg) {
		if(doPrint && msg != null) {
			System.out.print(msg);
		}
	}

	protected static void println(String msg) {
		if(doPrint && msg != null) {
			System.out.println(msg);
		}
	}

/* 
******************************************************
* BEGIN ZomicStatementTemplate and supporting classes
******************************************************
*/

	protected interface ZomicStatementTemplate <T extends ZomicStatement> {
		T generate();
	}
	
	protected interface IHaveAxisInfo {
										// e.g. red -2+
		String axisColor();				// red
		void axisColor(String s);
		
		String indexNumber();			// -2
		void indexNumber(String s);

		String handedness();			// +
		void handedness(String s);
		
		String indexFullName();			// -2+
	}

	protected class AxisInfo 
		implements IHaveAxisInfo
	{
		private String axisColor = "";
		private String indexNumber = "";
		private String handedness = "";

		Axis generate() {
			try {
				// TODO: move this check into a unit test instead of a runtime check.
				Axis axis = namingConvention.getAxis(axisColor, indexFullName() );
				String check = namingConvention.getName( axis );
				if ( axis != namingConvention.getAxis(axisColor, check ) ) {
					println( axisColor + " " + indexFullName() + " mapped to " + check );
				}
				return axis;
			} catch( RuntimeException ex ) {
				throw new RuntimeException( "bad axis specification: '" + axisColor + " " + indexFullName() + "'", ex);
			}
		}

		@Override
		public String axisColor() { return axisColor; }
		@Override
		public void axisColor(String s) { axisColor = s; }

		@Override
		public String indexNumber() { return indexNumber; }
		@Override
		public void indexNumber(String s) { indexNumber = s; }

		@Override
		public String handedness() { return handedness; }
		@Override
		public void handedness(String s) { handedness = s; }

		@Override
		public String indexFullName() { return indexNumber + handedness; }
	}

	private void setCurrentScale(int scale) { 
		((ScaleInfo)templates.peek()).scale = scale;
	}

	protected class ScaleInfo {
		public int ones = 1;
		public int phis = 0;
		public int scale = 1;

		AlgebraicNumber generate(IcosahedralSymmetry symmetry) {
			return symmetry.getField().createAlgebraicNumber( ones, phis, 1, scale );
		}
	}
	
	protected class ScaleTemplate 
		extends ScaleInfo
		implements ZomicStatementTemplate<Scale>
	{	
		@Override
		public Scale generate() {
			AlgebraicNumber algebraicNumber = generate(icosaSymmetry);
			return new Scale(algebraicNumber);
		}
	}

	protected class MoveTemplate 
		extends ScaleInfo
		implements ZomicStatementTemplate<Move>, IHaveAxisInfo
	{
		private final AxisInfo axisInfo = new AxisInfo();
		
		public int denominator = 1;
		public String sizeRef = null;

		public MoveTemplate() {
			scale = ZomicNamingConvention.MEDIUM;
		}

		private boolean isVariableLength = false;
		public boolean isVariableLength() { 
			return ( isVariableLength	// DJH New proposed alternative to size -99
					|| (-99 == scale)	// old variable size flag used only internally by strut resources.
					); 
		}
		public void isVariableLength(boolean is)  { isVariableLength = is; }

		AlgebraicNumber generate(String axisColor) {
			// validate 
			if ( denominator != 1 ) {
				Direction direction = icosaSymmetry.getDirection(axisColor);
				if ( direction == null || ! direction.hasHalfSizes()) {
					String msg = "half struts are not allowed on '" + axisColor + "' axes.";
					println(msg);
					throw new RuntimeException( msg );
				}
			} 
			// Zero is the "variable" length indicator. Used only by internal core strut resources
			if (isVariableLength()) { 
				return icosaSymmetry.getField().zero();
			}
			// adjustments per color
			int lengthFactor = 1;
			int scaleOffset = 0;
			switch (axisColor) {
				case "blue":
					lengthFactor = 2;
					break;
				case "green":
					lengthFactor = 2;
					break;
				case "yellow":
					scaleOffset = -1;
					break;
				case "purple":
					scaleOffset = -1;
					break;
				default:
					break;
			}
			// calculate
			return icosaSymmetry.getField().createAlgebraicNumber( 
					ones * lengthFactor, 
					phis * lengthFactor, 
					denominator, 
					scale + scaleOffset);
		}

		@Override
		public Move generate() {
			Axis axis = axisInfo.generate();
			AlgebraicNumber strutLength = generate(axisColor());
			return new Move(axis, strutLength);
		}

		@Override
		public String axisColor() { return axisInfo.axisColor; }
		@Override
		public void axisColor(String s) { axisInfo.axisColor(s); }
		
		@Override
		public String indexNumber() { return axisInfo.indexNumber; }
		@Override
		public void indexNumber(String s) { axisInfo.indexNumber(s); }
		@Override

		public String handedness() { return axisInfo.handedness; }
		@Override
		public void handedness(String s) { axisInfo.handedness(s); }


		@Override
		public String indexFullName() { return axisInfo.indexFullName(); }
	}

	protected class RotateTemplate 
		implements ZomicStatementTemplate<Rotate>, IHaveAxisInfo
	{
		private final AxisInfo axisInfo = new AxisInfo();
		public int steps = 1;
		
		@Override
		public Rotate generate() {
			Axis axis = axisInfo.generate();
			return new Rotate(axis, steps);
		}

		@Override
		public String axisColor() { return axisInfo.axisColor; }
		@Override
		public void axisColor(String s) { axisInfo.axisColor(s); }
		
		@Override
		public String indexNumber() { return axisInfo.indexNumber; }
		@Override
		public void indexNumber(String s) { axisInfo.indexNumber(s); }
		@Override

		public String handedness() { return axisInfo.handedness; }
		@Override
		public void handedness(String s) { axisInfo.handedness(s); }


		@Override
		public String indexFullName() { return axisInfo.indexFullName(); }
	}

	protected class ReflectTemplate 
		implements ZomicStatementTemplate<Reflect>, IHaveAxisInfo
	{
		private final AxisInfo axisInfo = new AxisInfo();
		private final boolean isThroughCenter;
				
		public ReflectTemplate(boolean isThruCenter) {
			isThroughCenter = isThruCenter;
		}
		
		@Override
		public Reflect generate() {
			Reflect result = new Reflect();
			if( !isThroughCenter ) {
				if( "".equals(axisColor()) && !"".equals(indexNumber()) ) {
					axisColor("blue");
				}
				Axis axis = axisInfo.generate();
				result.setAxis(axis);
			}
			return result;
		}

		@Override
		public String axisColor() { return axisInfo.axisColor; }
		@Override
		public void axisColor(String s) {
			if( !"blue".equals(s)) {
				enforceBlueAxis();
			} else {
				axisInfo.axisColor(s); 
			}
		}
		
		@Override
		public String indexNumber() { return axisInfo.indexNumber; }
		@Override
		public void indexNumber(String s) {
			// Old way silently removes any negative sign on the axis index
			// so we always reflect around the positive axis.
			// Don't know if that matters, but we'll do it the same way here.
			// Note that the "symmetry around axis" statement does not strip the sign.
			// but "symmetry through blueAxisIndex" does, just like ReflectTemplate.
			s = s.replaceFirst("-", "");
			axisInfo.indexNumber(s);
			if( isThroughCenter && !"".equals(indexNumber())) {
				axisColor("blue");
			}
		}
		
		@Override
		public String handedness() { return axisInfo.handedness; }
		@Override
		public void handedness(String s) {
			enforceBlueAxis();
		}

		@Override
		public String indexFullName() { return axisInfo.indexFullName(); }
		
		private void enforceBlueAxis() {
			throw new IllegalStateException("Only 'center' or blue axis indexes are allowed.");
		}
	}
	
	protected enum SymmetryModeEnum {
			Icosahedral,	// around the origin
			RotateAroundAxis,
			MirrorThroughBlueAxis,
			ReflectThroughOrigin
	}
	
	protected class SymmetryTemplate 
		implements ZomicStatementTemplate<Symmetry>, IHaveAxisInfo
	{
		private final AxisInfo axisInfo = new AxisInfo();
		private final SymmetryModeEnum symmetryMode;
				
		public SymmetryTemplate(SymmetryModeEnum mode) {
			symmetryMode = mode;
		}
		
		@Override
		public Symmetry generate() {
			// Rather than creating a new Symmetry statement here, apply the collected template parameters 
			// to the Symmetry statement that's already on the Statement stack collecting other statements in its body,
			// but don't pop it off the Statements stack here, just peek() for now.
			Symmetry result = (Symmetry) statements.peek();
			switch(symmetryMode) {
				case Icosahedral:
					break;
				case RotateAroundAxis:
				{
					Rotate rotate = new Rotate( null, -1 );
					Axis axis = axisInfo.generate();
					rotate.setAxis(axis);
					result.setPermute( rotate );
				}
					break;
				case MirrorThroughBlueAxis:
				{
					Reflect reflect = new Reflect();
					Axis axis = axisInfo.generate();
					reflect.setAxis(axis);
					result.setPermute( reflect );
				}
					break;
				case ReflectThroughOrigin:
					result.setPermute( new Reflect() );
					break;
				default:
					throw new IllegalStateException(
							"Unexpected SymmetryModeEnum: " + 
							symmetryMode == null
							? "<null>" 
							: symmetryMode.toString() 
					);
			}
			return result;
		}

		@Override
		public String axisColor() { return axisInfo.axisColor; }
		@Override
		public void axisColor(String s) { axisInfo.axisColor(s); }
		
		@Override
		public String indexNumber() { return axisInfo.indexNumber; }
		@Override
		public void indexNumber(String s) {
			// Note that the "symmetry around axis" statement does not strip the sign.
			// but "symmetry through blueAxisIndex" does, just like ReflectTemplate.
			// Don't know if that matters, but we'll do it the same way here.
			if(symmetryMode == SymmetryModeEnum.MirrorThroughBlueAxis) {
				s = s.replaceFirst("-", "");
			}
			axisInfo.indexNumber(s);
			if( symmetryMode == SymmetryModeEnum.MirrorThroughBlueAxis ) {
				axisColor("blue");
			}
		}
		
		@Override
		public String handedness() { return axisInfo.handedness; }
		@Override
		public void handedness(String s) { axisInfo.handedness(s); }

		@Override
		public String indexFullName() { return axisInfo.indexFullName(); }
	}

/* 
**********************************
* BEGIN Overriding Event Handlers 
**********************************
*/
	
	@Override public void enterProgram(ZomicParser.ProgramContext ctx) { 
		prepareStatement( new Walk() );
		prepareStatement( new Walk() );	// old parser had an extra Walk on the stack so we will too
	}
	
	@Override public void exitProgram(ZomicParser.ProgramContext ctx) { 
		commitLastStatement();	// old parser had an extra Walk on the stack so we will too
		if(!(statements.firstElement() instanceof Walk)) {
			throw new RuntimeException("We should always have a Walk by the time we get here!");		
		}
	}
	
	//@Override public void enterStmt(ZomicParser.StmtContext ctx) { }
	//@Override public void exitStmt(ZomicParser.StmtContext ctx) { }

	@Override public void enterCompound_stmt(ZomicParser.Compound_stmtContext ctx) {
		prepareStatement( new Walk() );
	}
	
	@Override public void exitCompound_stmt(ZomicParser.Compound_stmtContext ctx) {
		commitLastStatement();
	}
	
	//@Override public void enterDirectCommand(ZomicParser.DirectCommandContext ctx) { }
	//@Override public void exitDirectCommand(ZomicParser.DirectCommandContext ctx) { }
	
//	@Override public void enterNestedCommand(ZomicParser.NestedCommandContext ctx) { }
//	@Override public void exitNestedCommand(ZomicParser.NestedCommandContext ctx) { }
	
	@Override public void enterStrut_stmt(ZomicParser.Strut_stmtContext ctx) { 
		prepareTemplate( new MoveTemplate() );
	}
	
	@Override public void exitStrut_stmt(ZomicParser.Strut_stmtContext ctx) { 
		MoveTemplate template = (MoveTemplate) templates.pop();
		commit (template.generate());
	}
	
	//@Override public void enterLabel_stmt(ZomicParser.Label_stmtContext ctx) { }
	
	@Override public void exitLabel_stmt(ZomicParser.Label_stmtContext ctx) {
		commit( new Label(ctx.IDENT().getText() ) );
	}
	
	@Override public void enterScale_stmt(ZomicParser.Scale_stmtContext ctx) {
		prepareTemplate(new ScaleTemplate());
	}
	
	@Override public void exitScale_stmt(ZomicParser.Scale_stmtContext ctx) {
		if( ctx.scale != null ) {
			setCurrentScale(parseInt(ctx.scale));
		}
		ScaleTemplate template = (ScaleTemplate) templates.pop();
		commit (template.generate());
	}

	//@Override public void enterBuild_stmt(ZomicParser.Build_stmtContext ctx) { }
	
	@Override public void exitBuild_stmt(ZomicParser.Build_stmtContext ctx) { 
		commit( new Build(/*build*/ true, /*destroy*/ false) );
	}
	
	//@Override public void enterDestroy_stmt(ZomicParser.Destroy_stmtContext ctx) { }
	
	@Override public void exitDestroy_stmt(ZomicParser.Destroy_stmtContext ctx) { 
		commit( new Build(/*build*/ false, /*destroy*/ true) );
	}
	
	//@Override public void enterMove_stmt(ZomicParser.Move_stmtContext ctx) { }
	
	@Override public void exitMove_stmt(ZomicParser.Move_stmtContext ctx) { 
		commit( new Build(/*build*/ false, /*destroy*/ false) );
	}
	
	@Override public void enterRotate_stmt(ZomicParser.Rotate_stmtContext ctx) {
		prepareTemplate(new RotateTemplate());
	}

	@Override public void exitRotate_stmt(ZomicParser.Rotate_stmtContext ctx) {
		RotateTemplate template = (RotateTemplate) templates.pop();
		if(ctx.steps != null) {
			template.steps = parseInt(ctx.steps);
		}
		commit (template.generate());
	}
	
	@Override public void enterReflect_stmt(ZomicParser.Reflect_stmtContext ctx) {
		boolean isThruCenter = ctx.symmetry_center_expr().CENTER() != null;
		prepareTemplate(new ReflectTemplate(isThruCenter));
	}
	
	@Override public void exitReflect_stmt(ZomicParser.Reflect_stmtContext ctx) {
		ReflectTemplate template = (ReflectTemplate) templates.pop();
		commit( template.generate() );
	}
	
	@Override public void enterFrom_stmt(ZomicParser.From_stmtContext ctx) {
		prepareStatement( new Save(ZomicEventHandler.ACTION) );
		prepareStatement( new Walk() );
		commit( new Build(/*build*/ false, /*destroy*/ false) );
	}
	
	@Override public void exitFrom_stmt(ZomicParser.From_stmtContext ctx) {
		commitLastStatement();
		commitLastStatement();
	}
	
	@Override public void enterSymmetry_stmt(ZomicParser.Symmetry_stmtContext ctx) {
		SymmetryModeEnum symmetryMode = null;
		if( ctx.axis_expr() != null ) {
			symmetryMode = SymmetryModeEnum.RotateAroundAxis;
		} else if( ctx.symmetry_center_expr() == null ) {
			symmetryMode = SymmetryModeEnum.Icosahedral;
		} else if(ctx.symmetry_center_expr().blueAxisIndexNumber != null) {
			symmetryMode = SymmetryModeEnum.MirrorThroughBlueAxis;
		} else if(ctx.symmetry_center_expr().CENTER() != null) {
			symmetryMode = SymmetryModeEnum.ReflectThroughOrigin;
		} else {
			throw new IllegalStateException("Unexpected symmetry mode: " + ctx.getText());
		}
		// push a SymmetryTemplate on the Templates stack to collect the Symmetry parameters
		prepareTemplate(new SymmetryTemplate(symmetryMode));
		// push an actual Symmetry statement on the Statements stack to collect the body
		prepareStatement(new Symmetry());
	}

	@Override public void exitSymmetry_stmt(ZomicParser.Symmetry_stmtContext ctx) {
		SymmetryTemplate template = (SymmetryTemplate) templates.pop();
		// SymmetryTemplate.generate() will apply collected template params 
		// to the Symmetry statement that's already on the Statement stack collecting nested statements in its body.
		// The Symmetry statement that's returned is the same object that's still on the stetements stack,
		// so we can just ignore the return value here.
		template.generate();
		// Now commit the Symmetry statement and pop it off of the Statements stack.
		commitLastStatement();
	}
	
	@Override public void enterRepeat_stmt(ZomicParser.Repeat_stmtContext ctx) {
		// negative numbers are allowed but the sign is silently removed
		prepareStatement( new Repeat(abs(parseInt(ctx.count))) );
	}
	
	@Override public void exitRepeat_stmt(ZomicParser.Repeat_stmtContext ctx) {
		commitLastStatement();
	}
	
	@Override public void enterBranch_stmt(ZomicParser.Branch_stmtContext ctx) {
		prepareStatement(new Save(ZomicEventHandler.LOCATION) );
	}
	
	@Override public void exitBranch_stmt(ZomicParser.Branch_stmtContext ctx) {
		commitLastStatement();
	}
	
	@Override public void enterSave_stmt(ZomicParser.Save_stmtContext ctx) {
		int state = 0;
		switch(ctx.state.getText()) {
			case "orientation":
				state = ZomicEventHandler.ORIENTATION;
				break;
			case "scale":
				state = ZomicEventHandler.SCALE;
				break;
			case "location":
				state = ZomicEventHandler.LOCATION;
				break;
			case "build":
				state = ZomicEventHandler.ACTION;
				break;
			case "all":
				state = ZomicEventHandler.ALL;
				break;
			default:
				throw new UnsupportedOperationException("Unexpected save parameter: " + ctx.getText());
		}
		prepareStatement( new Save(state) );
	}
	
	@Override public void exitSave_stmt(ZomicParser.Save_stmtContext ctx) {
		commitLastStatement();
	}
	
	//@Override public void enterSymmetry_center_expr(ZomicParser.Symmetry_center_exprContext ctx) { }
	
	@Override public void exitSymmetry_center_expr(ZomicParser.Symmetry_center_exprContext ctx) {
		IHaveAxisInfo elements = (IHaveAxisInfo)templates.peek();
		if ( ctx.blueAxisIndexNumber != null ) { 
			elements.indexNumber(ctx.blueAxisIndexNumber.getText());
		}
	}
	
	//@Override public void enterStrut_length_expr(ZomicParser.Strut_length_exprContext ctx) { }
	
	@Override public void exitStrut_length_expr(ZomicParser.Strut_length_exprContext ctx) { 
		if(ctx.HALF() != null) {
			((MoveTemplate)templates.peek()).denominator = 2;
		}
	}
	
	//@Override public void enterHalf_size_expr(ZomicParser.Half_size_exprContext ctx) { }
	//@Override public void exitHalf_size_expr(ZomicParser.Half_size_exprContext ctx) { }
	
	//@Override public void enterSize_expr(ZomicParser.Size_exprContext ctx) { }
	//@Override public void exitSize_expr(ZomicParser.Size_exprContext ctx) { }
	
	//@Override public void enterExplicit_size_expr(ZomicParser.Explicit_size_exprContext ctx) { }
	
	@Override public void exitExplicit_size_expr(ZomicParser.Explicit_size_exprContext ctx) { 
		if( ctx.scale != null ) {
			setCurrentScale(parseInt(ctx.scale));
		}
		MoveTemplate template = (MoveTemplate)templates.peek();
		if( ctx.sizeRef != null ) {
			String sizeRef = ctx.sizeRef.getText();
			template.sizeRef = sizeRef;
			println("Ignoring undocumented sizeRef = '" + sizeRef + "'.");
		}
		if( ctx.isVariableLength != null ) {
			template.isVariableLength(true);
		}
	}
	
	@Override public void exitSizeShort(ZomicParser.SizeShortContext ctx) { 
		setCurrentScale(ZomicNamingConvention.SHORT);
	}
	
	@Override public void exitSizeLong(ZomicParser.SizeLongContext ctx) { 
		setCurrentScale(ZomicNamingConvention.LONG);
	}
	
	@Override public void exitSizeMedium(ZomicParser.SizeMediumContext ctx) { 
		setCurrentScale(ZomicNamingConvention.MEDIUM);
	}
	
	//@Override public void enterAxis_expr(ZomicParser.Axis_exprContext ctx) { }
	//@Override public void exitAxis_expr(ZomicParser.Axis_exprContext ctx) { }
	
	//@Override public void enterAxis_index_expr(ZomicParser.Axis_index_exprContext ctx) { }
	
	@Override public void exitAxis_index_expr(ZomicParser.Axis_index_exprContext ctx) {
		IHaveAxisInfo elements = (IHaveAxisInfo)templates.peek();
		elements.indexNumber(ctx.indexNumber.getText());
		if ( ctx.handedness != null ) { 
			elements.handedness(ctx.handedness.getText());
		}
	}
	
	//@Override public void enterAxis_name_expr(ZomicParser.Axis_name_exprContext ctx) { }

	/*
	 This convoluted looking code allows new colors and color aliases 
	 to be added to the language without overriding the event handler for each specific color.
	 It's a bit of overkill, but it's nice because it handles aliases automaatically. 
	 e.g. "pent" is an alias for "red". This mechanism handles translating the aliases.
	 The test for ending with strCONTEXT is to avoid using ErrorNodeImpl 
	 as when we parse an invalid color or some other unforeseen invalid context.
	*/
	@Override public void exitAxis_name_expr(ZomicParser.Axis_name_exprContext ctx) {
		final String strCONTEXT = "context";
		String colorContext = "Unexpected Axis Color: '" + ctx.getText() + "'.";
		if(ctx.children != null) {
			colorContext = ctx.getChild(0).getClass().getSimpleName().toLowerCase();
			if(colorContext.endsWith(strCONTEXT)) {
				colorContext = colorContext.replaceFirst(strCONTEXT, "");
				((IHaveAxisInfo)templates.peek()).axisColor(colorContext);
				println(colorContext);
				return;
			}
		} 
		println(colorContext);
		throw new RuntimeException( colorContext );
	} 

	@Override public void exitAlgebraic_number_expr(ZomicParser.Algebraic_number_exprContext ctx) { 
		ScaleInfo template = (ScaleInfo)templates.peek();
		template.ones = parseInt(ctx.ones);
		if(ctx.phis != null) {
			template.phis = parseInt(ctx.phis);
		}
	}
	
	@Override public void enterEveryRule(ParserRuleContext ctx) { 
		printContext(ctx, true);
	}
	
	@Override public void exitEveryRule(ParserRuleContext ctx) { 
		printContext(ctx, false);
	}
	
	//@Override public void visitTerminal(TerminalNode node) { }
	
	@Override public void visitErrorNode(ErrorNode node) { 
		String msg = node.getText();
		// TODO: Provide a list of expected tokens 
		// by catching a RecognitionException in the parser and using parser.getExpectedTokens()...
		// See http://stackoverflow.com/questions/25512770/antlr4-get-next-possible-matching-parser-rules-for-the-given-input
		// or "Altering and Redirecting ANTLR Error Messages" from section 9.2 of "The Definitive ANTLR 4 Reference"
		println("visitErrorNode: " + msg);
		commit( new Untranslatable(msg) );
	}

}
