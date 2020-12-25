
import { createStore, applyMiddleware, combineReducers } from 'redux'

import * as jre from './jre'
import * as files from './files'
import * as alerts from './alerts'
import * as cheerpj from './cheerpj'
import * as camera from './camera'
import * as lighting from './lighting'
import * as progress from './progress'
import * as jsweet from './jsweet'
import * as mesh from './mesh'
import * as commands from '../commands'
import * as workingPlane from './planes'
import * as models from './models'
import * as fields from '../fields'

const requiredBundles = { camera, lighting, fields }

let bundles
const urlParams = new URLSearchParams( window.location.search );
if ( urlParams.has( "editMode" ) ) {
  switch ( urlParams.get( "editMode" ) ) {

    case "plane":
      bundles = { ...requiredBundles, java: jsweet, models, workingPlane }
      break;
  
    default:
      bundles = { ...requiredBundles, java: jsweet, models, commands, files, alerts, progress }
      break;
  }
} else {
  bundles = { ...requiredBundles, java: cheerpj, files, alerts, progress }
}


export default ( middleware ) =>
{
  const names = Object.keys( bundles )

  const reducers = names.reduce( ( obj, key ) => {
    const reducer = bundles[key].reducer
    if ( reducer )
      obj[ key ] = reducer
    return obj
  }, {} )

  console.log( `bundle reducers: ${JSON.stringify( Object.keys( reducers ) )}` )

  const rootReducer = combineReducers( reducers )
  
  const store = createStore( rootReducer, applyMiddleware( ...middleware ) );
  
  // TODO: is there a better pattern than these inits?
  names.map( key => {
    const init = bundles[key].init
    if ( init ) {
      console.log( `bundle init: ${key}` )
      init( window, store )
    }
    return null
  } )

  return store
}