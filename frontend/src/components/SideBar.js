import React, { Component } from 'react'
import './styles/sidebar.css'

export default class SideBar extends Component {
    render() {
        return (
            <div className="d-flex" id="sidebar-wrapper">
                <div className="list-group list-group-flush">
                    {this.props.children}
                </div>
            </div>
        )
    }
}
