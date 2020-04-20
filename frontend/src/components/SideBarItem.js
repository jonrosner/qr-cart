import React, { Component } from 'react'
import './styles/sidebar.css'

export default class SideBarItem extends Component {
    render() {
        return (
            <a href={this.props.link} className="list-group-item list-group-item-action bg-light">{this.props.text}</a>
        )
    }
}
