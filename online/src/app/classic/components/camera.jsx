
import { createEffect } from 'solid-js';
import { SceneCanvas } from '../../../viewer/solid/scenecanvas.jsx';
import { useWorkerClient } from '../../../workerClient/index.js';
import { controllerAction } from '../../../workerClient/controllers-solid.js';
import { DummyTool } from '../tools/dummy.jsx';
import { InteractionToolProvider } from '../../../viewer/solid/interaction.jsx';

export const CameraControls = props =>
{
  const { state, rootController, isWorkerReady } = useWorkerClient();

  const scene = () => {
    let { camera, lighting, ...other } = state.trackballScene;
    const { camera: { lookDir, up }, lighting: { backgroundColor } } = state.scene;
    camera = { ...camera, lookDir, up }; // override just the orientation
    lighting = { ...lighting, backgroundColor }; // override just the background
    return ( { ...other, camera, lighting } );
  }

  // A special action that will result in state.trackballScene being set
  createEffect( () => isWorkerReady() && controllerAction( rootController(), 'connectTrackballScene' ) );

  return (
    <InteractionToolProvider>
      {/* provider and DummyTool just to get the desired cursor */}
      <DummyTool/>
      <div id='camera-controls' style={{ display: 'grid', 'grid-template-rows': 'min-content min-content' }}>
        <div id='camera-buttons' class='placeholder' style={{ 'min-height': '60px' }} >perspective | snap | outlines</div>
        <div id="ball-and-slider" style={{ display: 'grid', 'grid-template-columns': 'min-content 1fr' }}>
          <div id="camera-trackball" style={{ border: '1px solid' }}>
            <SceneCanvas scene={scene()} trackball={false} height="200px" width="240px" rotationOnly={true} />
          </div>
          <div id='zoom-slider' class='placeholder' style={{ 'min-height': '100px', 'min-width': '60px' }} >zoom</div>
        </div>
      </div>
    </InteractionToolProvider>
  )
}
