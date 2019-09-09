
import { ActionTypes } from "redux-simple-websocket"
import { OPEN_URL, CLOSE_VIEW } from '../actions'
import { DEFAULT_MODEL } from '../models/dodecahedron'

const reducer = (state = {
  modelUrl: "",
  connectionLive: false,
  instances: DEFAULT_MODEL.instances,
  shapes: DEFAULT_MODEL.shapes,
  lastError: null
}, action) => {
  switch (action.type) {

    case OPEN_URL:
      return {
        ...state,
        modelUrl: action.payload
      }

    case CLOSE_VIEW:
      return {
        ...state,
        modelUrl: "",
        connectionLive: false,
        instances: DEFAULT_MODEL.instances,
        shapes: DEFAULT_MODEL.shapes
      }

    case ActionTypes.WEBSOCKET_CONNECTED:
      return {
        ...state,
        connectionLive: true,
        instances: [],
        shapes: []
      }

    case ActionTypes.WEBSOCKET_ERROR:
      return {
        ...state,
        lastError: action.error
      }

    case ActionTypes.WEBSOCKET_DISCONNECTED:
      return {
        ...state,
        connectionLive: false
      }

    case ActionTypes.SEND_DATA_TO_WEBSOCKET:
      return {
        ...state
      }

    case ActionTypes.RECEIVED_WEBSOCKET_DATA:
      const parsed = action.payload;
      if ( parsed.render ) {
        console.log( parsed );
        if ( parsed.render === 'instance' ) {
          return {
            ...state,
            instances: [
              ...state.instances,
              parsed
            ]
          }
        } else if ( parsed.render === 'shape' ) {
          return {
            ...state,
            shapes: [
              ...state.shapes,
              {
                ...parsed,
                vertices: parsed.vertices.map( ([x,y,z]) => ({x,y,z}) )
              }
            ]
          }
        } else if ( parsed.render === 'delete' ) {
          let index = state.instances.findIndex( item => ( item.id === parsed.id ) )
          if ( index >= 0 ) {
            console.log( 'deleting instance' );
            return {
              ...state,
              instances: [
                ...state.instances.slice(0,index),
                ...state.instances.slice(index+1)
              ]
            }
          }
          return state
        } else {
          return state
        }
      } else {
        console.log( parsed.info );
        return state
      }

    default:
      return state
  }
}

export default reducer

