
import React, { useRef } from 'react'
import { useDispatch } from 'react-redux';

import IconButton from '@material-ui/core/IconButton'
import Tooltip from '@material-ui/core/Tooltip'
import FolderOpenRoundedIcon from '@material-ui/icons/FolderOpenRounded'
import Menu from '@material-ui/core/Menu'
import MenuItem from '@material-ui/core/MenuItem'
import Divider from '@material-ui/core/Divider';

import UrlDialog from './webloader.jsx'

const models = [
  {
    key: "vZomeLogo",
    label: "vZome Logo",
    description: "The vZome logo, one tetrahedral cell of the 4D 600-cell (cell-first projection)"
  },
  {
    key: "affineDodec",
    label: "Stretched Dodecahedron",
    description: "A regular dodecahedron stretched by a linear transformation"
  },
  {
    key: "120-cell",
    label: "Hyper-dodecahedron",
    description: "The 4D analogue of a dodecahedron, with 120 dodecahedral cells"
  },
  {
    key: "bluePlaneArches1",
    label: "Arched Rhombic Triacontahedron",
    description: "A sculpture built using just the blue planes"
  },
  {
    key: "C240",
    label: "C-240 Buckyball",
    description: "C-240 Buckyball"
  },
  {
    key: "orangePurpleChiral",
    label: "Orange and Purple Tangle",
    description: "A design by Brian Hall"
  },
  {
    key: "truncated5Cell",
    url: "https://www.vzome.com/models/2007/04-Apr/5cell/A4_3C.vZome",
    label: "Truncated 5-Cell",
    description: "Truncated 5-Cell"
  },
]

export const getModelURL = key => new URL( `./models/${key}.vZome`, window.location ) .toString();

export const OpenMenu = props =>
{
  const [anchorEl, setAnchorEl] = React.useState(null)
  const [showDialog, setShowDialog] = React.useState(false)
  const ref = useRef()
  const chooseFile = () => {
    setAnchorEl(null)
    ref.current.click()
  }

  const report = useDispatch();
  const openUrl = url => {
    if ( url && url.endsWith( ".vZome" ) ) {
      report( { type: 'URL_PROVIDED', payload: { url, viewOnly: false, } } );
    }
  }
  const openFile = file => {
    if ( file ) {
      report( { type: 'FILE_PROVIDED', payload: file } );
    }
  }

  // This can trigger an event cycle involving the "waiting" state,
  //  if we change to "[openUrl]", causing an endless repetition
  //  of this effect.
  // However, as-is, this does not have the desired effect of opening
  // the default URL, since openUrl will be a no-op until the controller
  // is created.
  // I'm taking it out, since an editor really should come up with a new document, anyway.
  //
  // useEffect( () => openUrl( url ), [] )

  const handleClickOpen = (event) => {
    setAnchorEl( event.currentTarget )
  }

  const handleSelectModel = model => {
    setAnchorEl(null)
    const { url, key } = model
    openUrl( url || getModelURL( key ), key );
  }

  const handleClose = () => {
    setAnchorEl(null)
  }

  const handleShowUrlDialog = () => {
    setAnchorEl(null)
    setShowDialog(true)
  }

  return (
    <>
      <Tooltip title="Open a design" aria-label="open">
        <IconButton color="inherit" aria-label="open" onClick={handleClickOpen}>
          <FolderOpenRoundedIcon fontSize="large"/>
        </IconButton>
      </Tooltip>
      <Menu
        anchorEl={anchorEl}
        keepMounted
        open={Boolean(anchorEl)}
        onClose={handleClose}
      >
        <MenuItem onClick={chooseFile} >Local vZome file
          <input className="FileInput" type="file" ref={ref}
            onChange={ (e) => {
                const selected = e.target.files && e.target.files[0]
                if ( selected )
                  openFile( selected )
              } }
            accept=".vZome" /> 
        </MenuItem>
        <MenuItem onClick={handleShowUrlDialog} >Remote vZome URL</MenuItem>
        <Divider />
        { models.map( (model) => (
          <MenuItem key={model.key} onClick={()=>handleSelectModel(model)}>{model.label}</MenuItem>
        ) ) }
      </Menu>
      <UrlDialog show={showDialog} setShow={setShowDialog} openDesign={openUrl} />
    </>
  )
}

