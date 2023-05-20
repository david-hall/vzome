import { com } from '../core-java.js';
import { JsProperties } from '../jsweet2js.js';
import { EditorController } from './editor.js';
import { PickingController } from './picking.js';
import { BuildPlaneController } from './buildplane.js';

class ControllerWrapper{
  
  constructor(path, name, controller, clientEvents) {
    this.__path = path;
    this.__name = name;
    this.controller = controller;
    this.__clientEvents = clientEvents;
    this.__changeTables = {};
    this.children = {};
  }

  createChildWrapper(name, controller) {
    const controllerPath = this.__path ? this.__path + ':' + name : name;
    const child = new ControllerWrapper(controllerPath, name, controller, this.__clientEvents);
    this.children[name] = child;

    const thisController = this.controller;
    // thisController .addPropertyListener( {
    //   propertyChange( pce )
    //   {
    //     const propName = pce .getPropertyName();
    //     if ( name === propName ) {
    //       child .controllerChange( thisController );
    //     }
    //   }
    // } );
    return child;
  }

  getSubControllerByNames(names) {
    if (!names || names.length === 0) {
      return this;
    }
    else {
      const name = names[0];
      let child = this.children[name];
      if (!child) {
        const controller = this.controller.getSubController(name);
        if (!controller) {
          console.log(`No subcontroller for path ${this.__path} ${name}`);
          return undefined;
        }
        child = this.createChildWrapper(name, controller);
      }
      return child.getSubControllerByNames(names.slice(1));
    }
  }

  fetchAndFirePropertyChange(propName, isList) {
    const value = isList ?
      this.controller.getCommandList(propName)
      : this.controller.getProperty(propName);
    this.__clientEvents.propertyChanged(this.__path, propName, value);
  }

  // This is only ever called on the root controller
  registerPropertyInterest(controllerPath, propName, changeName, isList) {
    const controllerNames = controllerPath ? controllerPath.split(':') : [];
    const wrapper = this.getSubControllerByNames(controllerNames);

    if (!wrapper)
      return; // Happens regularly on startup, when some properties are still undefined

    wrapper.registerPropertyInterest2(propName, changeName, isList);

    // In case the initial value is never going to change
    wrapper.fetchAndFirePropertyChange(propName, isList);
  }

  registerPropertyInterest2(propName, changeName, isList) {
    if (Object.entries(this.__changeTables).length === 0) {
      // First registered interest in any property for this controller
      this.controller.addPropertyListener(this);
    }
    if (!this.__changeTables[changeName]) {
      // First registered interest in this change name
      this.__changeTables[changeName] = { [propName]: isList };
    }
    else {
      const change = this.__changeTables[changeName];
      if (change[propName] === undefined) {
        // First registered interest in this property
        this.__changeTables[changeName] = { ...change, [propName]: isList };
      }
    }
  }

  doAction(controllerPath, action, parameters = {}) {
    const controllerNames = controllerPath ? controllerPath.split(':') : [];
    const wrapper = this.getSubControllerByNames(controllerNames);
    if (parameters && Object.keys(parameters).length !== 0)
      wrapper.controller.paramActionPerformed(null, action, new JsProperties(parameters));

    else
      wrapper.controller.actionPerformed(null, action);

    // TODO: this is pretty heavy-handed, sending the whole scene after every edit.
    //  That said, it may perform better than the incremental approach.
    this.renderScene();
  }

  controllerChange(parent) {
    this.controller = parent.getSubController(this.__name);
    // refresh all properties
    for (const changes of Object.values(this.__changeTables)) {
      for (const [propName, isList] of Object.entries(changes)) {
        this.fetchAndFirePropertyChange(propName, isList);
      }
    }
    // force re-fetch of all subcontrollers
    this.children = {};
  }

  propertyChange(pce) {
    const name = pce.getPropertyName();

    for (const [cName, changes] of Object.entries(this.__changeTables)) {
      if (name === cName) {
        // This is the reason for this.__changeTables, to "fan out" property changes
        for (const [propName, isList] of Object.entries(changes)) {
          this.fetchAndFirePropertyChange(propName, isList);
        }
      }
    }
  }
}
export const createControllers = (design, renderHistory, clientEvents) => {
  const { orbitSource, renderedModel, toolsModel, bookmarkFactory, history, symmetrySystems } = design;

  const controller = new EditorController(design, clientEvents); // this is the equivalent of DocumentController
  controller.setErrorChannel({
    reportError: (message, args) => {
      console.log('controller error:', message, args);
      if (message === com.vzome.desktop.api.Controller.UNKNOWN_ERROR_CODE) {
        const ex = args[0];
        clientEvents.errorReported(ex.message);
      }
      else
        clientEvents.errorReported(message);
    },
  });

  // This has similar function to the Java equivalent, but a very different mechanism
  const pickingController = new PickingController(renderedModel);
  controller.addSubController('picking', pickingController);

  // This has no desktop equivalent
  const buildPlaneController = new BuildPlaneController(renderedModel, orbitSource);
  controller.addSubController('buildPlane', buildPlaneController);

  const undoRedoController = new com.vzome.desktop.controller.UndoRedoController(history);
  controller.addSubController('undoRedo', undoRedoController);

  const bookmarkController = new com.vzome.desktop.controller.ToolFactoryController(bookmarkFactory);
  controller.addSubController('bookmark', bookmarkController);

  const strutBuilder = new com.vzome.desktop.controller.DefaultController(); // this is the equivalent of StrutBuilderController
  controller.addSubController('strutBuilder', strutBuilder);

  for (const [name, symmetrySystem] of Object.entries(symmetrySystems)) {
    const symmController = new com.vzome.desktop.controller.SymmetryController( strutBuilder, symmetrySystem, renderedModel );
    strutBuilder.addSubController(`symmetry.${name}`, symmController);
  }

  const toolsController = new com.vzome.desktop.controller.ToolsController(toolsModel);
  toolsController.addTool(toolsModel.get("bookmark.builtin/ball at origin"));
  strutBuilder.addSubController('tools', toolsController);

  const wrapper = new ControllerWrapper('', '', controller, clientEvents);

  // Not beautiful, but functional
  wrapper.getScene = (editId, before = false) => {
    return renderHistory.getScene(editId, before);
  };

  // TODO: fix this terrible hack!
  wrapper.renderScene = () => renderHistory.recordSnapshot('--END--', '--END--', []);

  // enable shape changes
  renderedModel .addListener( {
    shapesChanged: () => false, // this allows RenderedModel.setShapes() to not fail and re-render all the parts
    manifestationAdded: () => {},  // We don't need these incremental changes, since we'll batch render after
    manifestationRemoved: () => {},
    colorChanged: () => {},
    glowChanged: () => {},
  } );

  return wrapper;
};