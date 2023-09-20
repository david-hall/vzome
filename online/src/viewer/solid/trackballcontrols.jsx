
// modified copy from https://github.com/devinxi/vinxi/blob/1514f966d9cdcc2c19e2733a8c7bf03831f7ecf3/packages/solid-drei/src/OrbitControls.tsx

import { createEffect, createMemo, onCleanup } from "solid-js";
import { Vector3 } from "three";
import { useFrame, useThree } from "solid-three";
import { TrackballControls as TrackballControlsImpl } from "three-stdlib";

import { useRotation } from "./rotation.jsx";
import { useInteractionTool } from "./interaction.jsx";

export const TrackballControls = (props) =>
{
  const [ tool ] = useInteractionTool();
  const enabled = () => ( tool === undefined ) || tool().allowTrackball;
  const defaultCamera = useThree(({ camera }) => camera);
  const gl = useThree(({ gl }) => gl);
  const set = useThree(({ set }) => set);
  const get = useThree(({ get }) => get);
  const size = useThree(({ size }) => size);

  createEffect(() => {
    // if ( !props.rotationOnly ) console.log( 'TrackballControls createEffect 1 connect' );
    // SV: This effect is necessary so that we get correctly connected to the domElement
    //   *after* it has been connected to the document and assigned a valid size.
    if ( size().height < 0 ) // should never happen, just making a dependency
      console.log( 'height is', size().height ); // This is the change we care about.
    trackballControls() .connect( gl().domElement );
  });

  const thisCamera = () => props.camera || defaultCamera();

  const trackballControls = createMemo( () => {
    // if ( !props.rotationOnly ) console.log( 'TrackballControls CREATION memo' );
    return new TrackballControlsImpl( thisCamera(), gl().domElement );
  } );

  useFrame(() => {
    let controls = trackballControls();
    if ( controls.enabled ) {
      // if ( !props.rotationOnly ) console.log( 'TrackballControls frame update' );
      controls.update();
    }
  });

  createEffect( () => {
    // if ( !props.rotationOnly ) console.log( `TrackballControls enabled or disabled` );
    trackballControls() .enabled = enabled();
  });

  const { lastRotation, publishRotation } = useRotation();

  createEffect(() =>
  {
    const controls = trackballControls();

    controls.staticMoving = true;
    if ( props.rotationOnly ) {
      controls.noZoom = true;
      controls.noPan = true;
    } else {
      controls.zoomSpeed = props.zoomSpeed;
      controls.panSpeed = props.panSpeed;
    }
    controls.rotateSpeed = props.rotateSpeed;

    controls.connect( gl().domElement );

    controls.addEventListener( 'change', (evt) => {
      if ( ! publishRotation ) return;
      // filter out pan and zoom changes
      // HACK! Assumes knowledge of TrackballControls internals
      if ( controls._state !== 0 && controls._state !== 3 ) // ROTATE, TOUCH_ROTATE
        return;
      const { _lastAxis, _lastAngle } = controls; // the rotation change
      // if ( !props.rotationOnly ) console.log( 'TrackballControls publishing rotation %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%' );
      publishRotation( thisCamera().quaternion, thisCamera() );
    } );

    if (props.onStart) controls.addEventListener("start", props.onStart);
    if (props.onChange) controls.addEventListener("change", props.onChange);
    if (props.onEnd)   controls.addEventListener("end", props.onEnd);

    onCleanup(() => {
      if (props.onStart) controls.removeEventListener("start", props.onStart);
      if (props.onChange) controls.removeEventListener("change", props.onChange);
      if (props.onEnd)   controls.removeEventListener("end", props.onEnd);
      controls.dispose();
    });
  });

  createEffect(() => {
    const [ x, y, z ] = props.target;
    // if ( !props.rotationOnly ) console.log( 'trackballControls target', x, y, z );
    trackballControls() .target .set( x, y, z );
  });

  createEffect(() => {
    // if ( !props.rotationOnly ) console.log( 'TrackballControls createEffect 5 lifecycle' );
    if (props.makeDefault) {
      const old = get()().controls;
      set()({ controls: trackballControls() });
      onCleanup(() => set()({ controls: old }));
    }
  });

  const _eye = new Vector3(), _up = new Vector3(), _target = new Vector3();
  createEffect( () => {
    // if ( !props.rotationOnly ) console.log( 'TrackballControls createEffect 6 lastRotation' );
    if ( ! lastRotation ) return;
    let { quaternion, sourceCamera } = lastRotation();
    const camera = thisCamera();
    if ( sourceCamera && sourceCamera !== camera ) {
      // console.log( `${camera.vzomeName} receiving rotation from ${sourceCamera.vzomeName}` );
      // Mimic the logic of TrackballControls.update() with rotate(), but for an absolute quaternion
      _target.set( ...props.target );
      _eye.copy( camera.position ).sub( _target );
      const len = _eye.length();
      _eye .set( 0, 0, 1 ) .applyQuaternion( quaternion ) .multiplyScalar( len );
      _up .set( 0, 1, 0 ) .applyQuaternion( quaternion );
      camera.up.copy( _up );
      camera.position.addVectors( _target, _eye );
      camera.lookAt( _target );
    }
  });

  return null;
};