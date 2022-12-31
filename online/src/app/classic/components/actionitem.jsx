
import MenuItem from "@suid/material/MenuItem"
import Typography from "@suid/material/Typography";
import ListItemText from "@suid/material/ListItemText";
import { controllerAction, subController } from "../controllers-solid";

const isMac = navigator.userAgentData?.platform === 'macOS';

export const createActionItem = ( controller, fallback, doClose ) => ( props ) =>
{
  const sub = subController( controller, props.controller || fallback );
  const doAction = () => 
  {
    doClose();
    controllerAction( sub, props.action );
  }

  let modifiers = props.mods;
  if ( !isMac )
    modifiers = modifiers .replace( '⌘', '⌃' );
  if ( !props.disabled ) {
    const targetCodes = props.code?.split( '|' ) || ( props.key && [ "Key" + props.key.toUpperCase() ] );
    if ( targetCodes ) {
      console.log( targetCodes );
      const hasMeta = !! modifiers ?.includes( '⌘' );
      const hasControl = !! modifiers ?.includes( '⌃' );
      const hasShift = !! modifiers ?.includes( '⇧' );
      const hasOption = !! modifiers ?.includes( '⌥' );
      document .addEventListener( "keydown", evt => {
        if ( targetCodes .indexOf( evt.code ) < 0 )
          return;
        if ( hasMeta !== evt.metaKey )
          return;
        if ( hasControl !== evt.ctrlKey )
          return;
        if ( hasShift !== evt.shiftKey )
          return;
        if ( hasOption !== evt.altKey )
          return;
        evt .preventDefault();
        doAction();
      } );
    }
  }

  return (
    <MenuItem disabled={props.disabled} onClick={doAction}>
      <ListItemText>{props.label}</ListItemText>
      <Show when={props.code || props.key} >
        <Typography variant="body2" color="text.secondary">
          { props.code? "⌫" : modifiers + props.key }
        </Typography>
      </Show>
    </MenuItem>
  );
}
