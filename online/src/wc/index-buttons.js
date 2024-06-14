
const debug = false;

class VZomeViewerIndexButton extends HTMLElement
{
  #next;
  #viewerId;
  #viewer;
  #loadCamera;
  #button;
  #maxSceneIndex;

  constructor( next=true )
  {
    super();
    this.#next = next;
    this.#loadCamera = false;
  }

  #disabled( index )
  {
    if ( this.#next )
      return index === this.#maxSceneIndex;
    else
      return index === 0;
  }

  connectedCallback()
  {
    if ( !! this.#viewerId ) {
      this.#viewer = document .querySelector( `#${this.#viewerId}` );
      if ( ! this.#viewer ) {
        console.error( `No vzome-viewer with id "${this.#viewerId}" found.` );
      } else if ( this.#viewer .nextScene === undefined ) {
        console.error( `Element with id "${this.#viewerId}" is not a vzome-viewer.` );
        return;
      }
    }
    if ( ! this.#viewer ) {
      this.#viewer = document .querySelector( 'vzome-viewer' );
    }
    if ( ! this.#viewer ) {
      console.error( `No vzome-viewer found.` );
      return;
    }

    this.#button = document .createElement( 'button' );
    this.#button .textContent = this.getAttribute( 'label' );
    this .appendChild( this.#button );
    this.#button.classList .add( 'vzome-viewer-index-button' );

    this.#viewer .addEventListener( "vzome-design-rendered", (e) => {
      const { index } = e.detail;
      if ( this.#disabled( index ) )
        this.#button .setAttribute( 'disabled', 'true' );
      else
        this.#button .removeAttribute( 'disabled' );
    } );
    this.#viewer .addEventListener( "vzome-scenes-discovered", (e) => {
      const titles = e.detail;
      this.#maxSceneIndex = titles.length - 1;
    } );

    const loadParams = { camera: this.#loadCamera };
    this.#button .addEventListener( "click", () => this.#next? this.#viewer .nextScene( loadParams ) : this.#viewer .previousScene( loadParams ) );
  }

  static get observedAttributes()
  {
    return [ "viewer", "load-camera" ];
  }

  attributeChangedCallback( attributeName, _oldValue, _newValue )
  {
    debug && console.log( 'VZomeViewerIndexButton attribute changed' );
    switch (attributeName) {

    case "viewer":
      this.#viewerId = _newValue;
      break;

    case "load-camera":
      this.#loadCamera = _newValue === 'true';
      break;
    }
  }
}

export class VZomeViewerNextButton extends VZomeViewerIndexButton
{
  constructor()
  {
    super( true );
  }
}

export class VZomeViewerPrevButton extends VZomeViewerIndexButton
{
  constructor()
  {
    super( false );
  }
}