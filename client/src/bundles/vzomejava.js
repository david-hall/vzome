
import { FILE_LOADED } from './files'
import DEFAULT_MODEL from '../models/logo'
import { writeTextFile, callStaticMethod, callObjectMethod, createWriteableFile } from './jre'
import { startProgress, stopProgress } from './progress'

// These are dispatched from Java
const SHAPE_DEFINED    = 'SHAPE_DEFINED'
const INSTANCE_ADDED   = 'INSTANCE_ADDED'
const INSTANCE_COLORED = 'INSTANCE_COLORED'
const INSTANCE_REMOVED = 'INSTANCE_REMOVED'
export const MODEL_LOADED     = 'MODEL_LOADED'
const LOAD_FAILED      = 'LOAD_FAILED'

const CONTROLLER_RETURNED = 'CONTROLLER_RETURNED'

export const exportTriggered = ( extension, message ) => async (dispatch, getState) =>
{
  dispatch( startProgress( message ) )
  const controller = getState().vzomejava.controller
  const path = "/" + getState().vzomejava.fileName.replace( ".vZome", "." + extension )
  const file = await createWriteableFile( path )
  callObjectMethod( controller, "doFileAction", "export." + extension, file ).then( () =>
  {
    dispatch( stopProgress() )
  })
}

const normalize = ( instance ) =>
{
  const pos = instance.position
  const quat = instance.rotation
  const rotation = quat? [ quat.x, quat.y, quat.z, quat.w ] : [ 1, 0, 0, 0 ]
  return { ...instance, shapeId: instance.shape, position: [ pos.x, pos.y, pos.z ], rotation }
}

const initialState = {
  renderingOn: true,
  controller: undefined,
  fileName: undefined,
  shapes: DEFAULT_MODEL.shapes,
  instances: DEFAULT_MODEL.instances.map( normalize ),
  previous: DEFAULT_MODEL.instances.map( normalize )
}

export const reducer = ( state = initialState, action ) => {
  switch (action.type) {

    case FILE_LOADED:
      return {
        ...state,
        fileName: action.payload.name,
        renderingOn: false,
        controller: undefined,
        instances: [],
        previous: state.instances
      }

    case SHAPE_DEFINED:
      // note, we don't need to map the vertices any more
      return {
        ...state,
        shapes: [
          ...state.shapes,
          action.payload
        ]
      }
  
    case INSTANCE_ADDED:
      return {
        ...state,
        instances: [
          ...state.instances,
          normalize( action.payload )
        ]
      }

    case INSTANCE_COLORED: {
      let index = state.instances.findIndex( item => ( item.id === action.payload.id ) )
      if ( index >= 0 ) {
        return {
          ...state,
          instances: [
            ...state.instances.slice(0,index),
            {
              ...state.instances[ index ],
              color: action.payload.color
            },
            ...state.instances.slice(index+1)
          ]
        }
      }
      return state
    }

    case INSTANCE_REMOVED: {
      let index = state.instances.findIndex( item => ( item.id === action.payload.id ) )
      if ( index >= 0 ) {
        return {
          ...state,
          instances: [
            ...state.instances.slice(0,index),
            ...state.instances.slice(index+1)
          ]
        }
      }
      return state
    }

    case MODEL_LOADED:
      return {
        ...state,
        renderingOn : true,
        previous: []
      }

    case CONTROLLER_RETURNED:
      return {
        ...state,
        controller: action.payload
      }
  
    case LOAD_FAILED:
      return {
        ...initialState
      }
  
    default:
      return state
  }
}

export const middleware = store => next => async action => 
{
  if ( action.type === FILE_LOADED ) {
    store.dispatch( startProgress( "Parsing vZome model..." ) )
    const path = "/str/" + action.payload.name
    writeTextFile( path, action.payload.text )
    callStaticMethod( "com.vzome.cheerpj.JavascriptClientShim", "openFile", path )
      .then( (controller) =>
      {
        store.dispatch( {
          type: CONTROLLER_RETURNED,
          payload: controller
        } )
        store.dispatch( stopProgress() )
      })
  }
  
  return next( action )
}

export const supportsEdits = false

const filterInstances = ( shape, instances ) =>
{
  return instances.filter( instance => instance.shapeId === shape.id )
}

export const sortedShapes = ( { vzomejava } ) =>
{
  const instances = vzomejava.renderingOn? vzomejava.instances : vzomejava.previous
  return vzomejava.shapes.map( shape => ( { shape, instances: filterInstances( shape, instances ) } ) )
}