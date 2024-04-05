
const debug = false;

class VZomeViewerIndexButton extends HTMLButtonElement
{
  #next;
  #viewerId;
  #viewer;

  constructor( next=true )
  {
    self = super();
    this.#next = next;
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
    const loadParams = { camera: false };
    self .addEventListener( "click", () => this.#next? this.#viewer .nextScene( loadParams ) : this.#viewer .previousScene( loadParams ) );
  }

  static get observedAttributes()
  {
    return [ "viewer" ];
  }

  attributeChangedCallback( attributeName, _oldValue, _newValue )
  {
    debug && console.log( 'VZomeViewerIndexButton attribute changed' );
    switch (attributeName) {

    case "viewer":
      this.#viewerId = _newValue;
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