
import { createEffect, createSignal } from 'solid-js';
import { render } from 'solid-js/web';

import { Button } from "@kobalte/core/button";
import { Switch } from "@kobalte/core/switch";

import { CameraProvider, DesignViewer } from '../../viewer/index.jsx';
import { ViewerProvider, useViewer } from '../../viewer/context/viewer.jsx';
import { WorkerProvider, useWorkerClient } from '../../viewer/context/worker.jsx';

import { instructionsCSS } from "./zometool.css.js";
import { urlViewerCSS } from "../../viewer/urlviewer.css.js";

import { ZometoolPartsElement } from './parts-list.jsx';
import { ZometoolProductsElement } from './products-list.jsx';
import { normalizeBOM } from './bom.js';

const debug = false;

const parts_catalog_url = 'https://zometool.github.io/vzome-sharing/metadata/zometool-parts.json';
const partsPromise = fetch( parts_catalog_url ) .then( response => response.text() ) .then( text => JSON.parse( text ) );

const StepControls = props =>
{
  const { scenes, requestScene } = useViewer();
  const [ index, setIndex ] = createSignal( 1 );
  const [ maxIndex, setMaxIndex ] = createSignal( 0 );
  const atStart = () => index() === 1;  // NOTE: scene 0 is the default scene, which we ignore
  const atEnd = () => index() === maxIndex();

  createEffect( () => setMaxIndex( scenes?.length - 1 ) );

  createEffect( () => {
    if ( props.show ) {
      requestScene( '#' + index(), { camera: false } );
    } else {
      requestScene( '#' + maxIndex(), { camera: true } );
    }
  } );

  const change = ( delta ) => evt =>
  {
    let newIndex;
    if ( delta === 0 ) {
      newIndex = 1;
    } else if ( delta === -2 ) {
      newIndex = maxIndex();
    } else {
      newIndex = index() + delta;
    }
    setIndex( newIndex );
  }

  const { subscribeFor } = useWorkerClient();
  subscribeFor( 'BOM_CHANGED', bom => {
    partsPromise .then( parts => {
      const detail = normalizeBOM( bom, parts );
      props.dispatch( new CustomEvent( 'zometool-instructions-loaded', { detail } ) );
    });
  } );

  return (
    <div class="step-buttons">
      <Show when={props.show}>
        <Button disabled={atStart()} class='step-button limit-step' tooltip='First step'    onClick={ change( 0 ) } >
          <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false" class="step-button-svg">
            <path d="M24 0v24H0V0h24z" fill="none" opacity=".87"></path>
            <path d="M17.7 15.89L13.82 12l3.89-3.89c.39-.39.39-1.02 0-1.41-.39-.39-1.02-.39-1.41 0l-4.59 4.59c-.39.39-.39 1.02 0 1.41l4.59 4.59c.39.39 1.02.39 1.41 0 .38-.38.38-1.02-.01-1.4zM7 6c.55 0 1 .45 1 1v10c0 .55-.45 1-1 1s-1-.45-1-1V7c0-.55.45-1 1-1z"></path>
          </svg>
        </Button>
        <Button disabled={atStart()} class='step-button' tooltip='Previous step' onClick={ change( -1 ) } >
          <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false" class="step-button-svg">
            <path d="M14.91 6.71c-.39-.39-1.02-.39-1.41 0L8.91 11.3c-.39.39-.39 1.02 0 1.41l4.59 4.59c.39.39 1.02.39 1.41 0 .39-.39.39-1.02 0-1.41L11.03 12l3.88-3.88c.38-.39.38-1.03 0-1.41z"></path>
          </svg>
        </Button>
        <h1 class='step-number'>{index()}</h1>
        <Button disabled={atEnd()}   class='step-button' tooltip='Next step'     onClick={ change( +1 ) } >
          <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false" class="step-button-svg">
            <path d="M9.31 6.71c-.39.39-.39 1.02 0 1.41L13.19 12l-3.88 3.88c-.39.39-.39 1.02 0 1.41.39.39 1.02.39 1.41 0l4.59-4.59c.39-.39.39-1.02 0-1.41L10.72 6.7c-.38-.38-1.02-.38-1.41.01z"></path>
          </svg>
        </Button>
        <Button disabled={atEnd()}   class='step-button limit-step' tooltip='Last step'     onClick={ change( -2 ) } >
          <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false" class="step-button-svg">
            <path d="M0 0h24v24H0V0z" fill="none" opacity=".87"></path>
            <path d="M6.29 8.11L10.18 12l-3.89 3.89c-.39.39-.39 1.02 0 1.41.39.39 1.02.39 1.41 0l4.59-4.59c.39-.39.39-1.02 0-1.41L7.7 6.7c-.39-.39-1.02-.39-1.41 0-.38.39-.38 1.03 0 1.41zM17 6c.55 0 1 .45 1 1v10c0 .55-.45 1-1 1s-1-.45-1-1V7c0-.55.45-1 1-1z"></path>
          </svg>
        </Button>
      </Show>
    </div>
  );
}

const ZometoolInstructions = props =>
{
  const [ steps, setSteps ] = createSignal( false );
  const toggleSteps = () => setSteps( v => !v );

  return (
    <CameraProvider>
      <WorkerProvider>
        <ViewerProvider config={{ url: props.url, preview: true, debug: false, showScenes: false, labels: true, source: true }}>
          <div class='zometool-instructions'>

            <Switch class="switch" checked={steps()} onChange={toggleSteps}>
              <Switch.Label class="step_switch__label">Show Build Steps</Switch.Label>
              <Switch.Input class="switch__input" />
              <Switch.Control class="switch__control">
                <Switch.Thumb class="switch__thumb" />
              </Switch.Control>
            </Switch>

            <DesignViewer config={ { ...props.config, download: !steps(), allowFullViewport: true } }
                componentRoot={props.componentRoot}
                height="100%" width="100%" >
            </DesignViewer>

            <StepControls show={steps()} dispatch={props.dispatch} />
          </div>
        </ViewerProvider>
      </WorkerProvider>
    </CameraProvider>
  );
}

const renderComponent = ( url, container, dispatch ) =>
  {
    const bindComponent = () =>
    {
      return (
        <ZometoolInstructions url={url} dispatch={dispatch} >
        </ZometoolInstructions>
      );
    }
  
    container .appendChild( document.createElement("style") ).textContent = urlViewerCSS;
    // Apply external override styles to the shadow dom
    // const linkElem = document.createElement("link");
    // linkElem .setAttribute("rel", "stylesheet");
    // linkElem .setAttribute("href", "./zometool-styles.css");
    // container .appendChild( linkElem );
  
    render( bindComponent, container );
  }
  
class ZometoolInstructionsElement extends HTMLElement
{
  #container;
  #url;

  constructor()
  {
    super();
    const root = this.attachShadow({ mode: "open" });

    root.appendChild( document.createElement("style") ).textContent = instructionsCSS;
    this.#container = document.createElement("div");
    root.appendChild( this.#container );

    debug && console.log( 'ZometoolInstructionsElement constructed' );
  }

  connectedCallback()
  {
    debug && console.log( 'ZometoolInstructionsElement connected' );

    renderComponent( this.#url, this.#container, evt => this.dispatchEvent(evt) );
  }

  static get observedAttributes()
  {
    return [ "src", ];
  }

  // This callback can happen *before* connectedCallback()!
  attributeChangedCallback( attributeName, _oldValue, _newValue )
  {
    debug && console.log( 'ZometoolInstructionsElement attribute changed' );
    switch (attributeName) {

    case "src":
      this.#url = new URL( _newValue, window.location ) .toString();
    }
  }

  getParts()
  {
    debug && console.log( 'ZometoolInstructionsElement.getParts called' );
    /*

    I have a few problems to solve before this will be useful.

    1. The "orbit" and "name" properties exported in .shapes.json (by desktop vZome)
        are not carried over from the worker to the client.  (See 4 below.)

    2. Whatever metadata comes to the client should come when there is no preview, also,
        though this scenario is not likely.  I may just consider this a non-requirement.

    3. The .shapes.json exported from vZome Online classic does NOT carry those properties!
        Again, since Paul is the expected author, and he is unlikely to use Online for a
        while, this can be deferred.

    4. The value space of the "name" property is not exactly useful.  I should normalize
        to Zometool standard part IDs on the worker.

    5. vZome does not constrain colors, so unless Paul starts using the "orbits as palette"
        idea, we'll have trouble recognizing colored parts.

    */
  }
}

customElements.define( "zometool-instructions", ZometoolInstructionsElement );
customElements.define( "zometool-parts-required", ZometoolPartsElement );
customElements.define( "zometool-covering-products", ZometoolProductsElement );


