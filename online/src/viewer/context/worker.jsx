
import { createContext, useContext } from "solid-js";

import { createWorkerStore } from "../util/controllers-solid.js";
import { fetchDesign } from "../util/actions.js";

const createWorker = () =>
{
  // trampolining to work around worker CORS issue
  // see https://github.com/evanw/esbuild/issues/312#issuecomment-1025066671
  const workerPromise = import( "../../worker/vzome-worker-static.js" )
    .then( module => {
      const blob = new Blob( [ `import "${module.WORKER_ENTRY_FILE_URL}";` ], { type: "text/javascript" } );
      const worker = new Worker( URL.createObjectURL( blob ), { type: "module" } );
      worker.onmessage = onWorkerMessage;
      return worker;
    } );

  const sendToWorker = event =>
  {
      workerPromise.then( worker => {
        // console.log( `Message sending to worker: ${JSON.stringify( event, null, 2 )}` );
        worker .postMessage( event );  // send them all, let the worker filter them out
      } )
      .catch( error => {
        console.log( error );
        console.log( "The worker is not available" );
        onWorkerError( 'The worker is not available.  Module workers are supported in the latest versions of most browsers.  Please update your browser.' );
      } );
  }

  const subscribers = [];

  const subscribe = subscriber => subscribers .push( subscriber );

  const onWorkerMessage = message =>
    subscribers .forEach( subscriber => subscriber .onWorkerMessage( message.data ) );
  const onWorkerError = message =>
    subscribers .forEach( subscriber => subscriber .onWorkerError( message ) );

  return { sendToWorker, subscribe };
}

const WorkerStateContext = createContext( {} );

const WorkerStateProvider = ( props ) =>
{
  const workerClient = props.store || createWorkerStore( createWorker() );
  const { url } = props.config || {};
  url && workerClient.postMessage( fetchDesign( url, props.config ) );
  
  return (
    <WorkerStateContext.Provider value={ { ...workerClient } }>
      {props.children}
    </WorkerStateContext.Provider>
  );
}

const useWorkerClient = () => { return useContext( WorkerStateContext ); };

export { createWorker, WorkerStateProvider, useWorkerClient };