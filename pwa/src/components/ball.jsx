import React from 'react'

function Ball( { onClick, geom, material, position=[0,0,0] } ) {
  const ballClick = (e) => {
    e.stopPropagation()
    onClick( position )
  }
  return (
    <mesh position={position} geometry={geom} material={material}
      onPointerOver={e => console.log('hover')}
      onPointerOut={e => console.log('unhover')}
      onClick={e=>ballClick(e)}>
    </mesh>
  )
}

export default Ball
