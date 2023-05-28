
import DialogContent from "@suid/material/DialogContent"
import Dialog from "@suid/material/Dialog"
import DialogTitle from "@suid/material/DialogTitle"
import DialogActions from "@suid/material/DialogActions"
import Button from "@suid/material/Button"

import { subController, controllerProperty } from "../../../workerClient/controllers-solid.js";
import { OrbitPanel } from "./orbitpanel.jsx";

const OrbitsDialog = props =>
{
  const allOrbits = () => controllerProperty( props.controller, 'orbits', 'orbits', true );
  const availableOrbits = () => subController( props.controller, 'availableOrbits' );
  const orbits = () => controllerProperty( availableOrbits(), 'orbits', 'orbits', true );
  const snapOrbits = () => subController( props.controller, 'snapOrbits' );
  const lastSelected = () => controllerProperty( buildOrbits(), 'selectedOrbit', 'orbits', false );

  return (
    <Dialog onClose={ () => props.close() } open={props.open} maxWidth='md' fullWidth='true'>
      <DialogTitle id="orbits-dialog">Direction Configuration</DialogTitle>
      <DialogContent>
        <div style={{ display: 'grid', 'grid-template-columns': '1fr 1fr', 'min-width': '550px' }}>
          <OrbitPanel orbits={allOrbits()} controller={availableOrbits()} lastSelected={lastSelected()}
            label="available directions" style={{ height: '100%' }} />
          <OrbitPanel orbits={orbits()} controller={snapOrbits()} lastSelected={lastSelected()}
            label="snap directions" style={{ height: '100%' }} />
        </div>
      </DialogContent>
      <DialogActions>
        <Button size="small" onClick={ ()=>props.close() } color="primary">
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export { OrbitsDialog };
