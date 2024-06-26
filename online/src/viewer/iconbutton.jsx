
import { Tooltip } from "@kobalte/core/tooltip";

export const IconButton = (props) =>
{
  return (
    <Tooltip>
      <Tooltip.Trigger class={`${props.class} corner__icon__button`} onclick={props.onClick}>
        {props.children}
      </Tooltip.Trigger>
      <Tooltip.Portal mount={props.root}>
        <Tooltip.Content class="iconbutton__content">
          <Tooltip.Arrow />
          <p>{props.tooltip}</p>
        </Tooltip.Content>
      </Tooltip.Portal>
    </Tooltip>
  );
}
