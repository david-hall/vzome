
import React from 'react';

export default class ModelCanvas extends React.Component {

  static defaultProps = { width: 800, height: 600, scale: 5 };

  constructor(props) {
    super(props);
    this.state = {
      segments: []
    };
  }
  
  renderSegment(segment) {
    console.log( "segment: " + JSON.stringify(segment) );
    this.setState({ 
      segments: this.state.segments.concat([segment])
    })
  }
  
  render() {
  
    const center = { x: this.props.width/2, y: this.props.height/2 };

    return (
      <svg width={this.props.width} height={this.props.height}>
        <rect x="0" y="0" width={this.props.width} height={this.props.height}
          style={{stroke: "#000088", strokeWidth: 3, fill: 'none'}} />
        { this.state.segments.map((segment, i) => 
          <line
            x1={this.props.scale*segment.start.x + center.x}
            y1={this.props.scale*segment.start.y + center.y}
            x2={this.props.scale*segment.end.x + center.x}
            y2={this.props.scale*segment.end.y + center.y}
            strokeWidth="2" stroke={segment.color}/> ) }
      </svg>
    )
  }
}
