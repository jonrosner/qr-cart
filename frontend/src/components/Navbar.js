import React, { Component } from 'react'
import Navigation from 'react-bootstrap/Navbar'
import Nav from 'react-bootstrap/Nav'
import NavDropdown from 'react-bootstrap/NavDropdown'
import './styles/navbar.css'

export default class Navbar extends Component {
    render() {
        return (
            <Navigation variant="dark" bg="dark" expand="md">
                <Navigation.Brand href="/"><img src="/logo.png" class="logo-left" alt="QR Cart" title="QR Cart" /> QR Cart</Navigation.Brand>
                <Navigation.Toggle aria-controls="basic-navbar-nav" />
                <Navigation.Collapse id="basic-navbar-nav">
                    <Nav className="mr-auto">
                        <Nav.Link href="/stores">Stores</Nav.Link>
                        {/* <Nav.Link href="/reservations">Reservations</Nav.Link> */}
                    </Nav>

                    <Nav className="justify-content-end">
                        <NavDropdown
                            title={
                                <svg className="bi bi-bell-fill" width="1em" height="1em" viewBox="0 0 16 16" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M8 16a2 2 0 002-2H6a2 2 0 002 2zm.995-14.901a1 1 0 10-1.99 0A5.002 5.002 0 003 6c0 1.098-.5 6-2 7h14c-1.5-1-2-5.902-2-7 0-2.42-1.72-4.44-4.005-4.901z" />
                                </svg>
                            }
                            id="notification-dropdown"
                            alignRight>
                        </NavDropdown>
                        <NavDropdown 
                            title={<img className="profilePic" src="https://secure.gravatar.com/avatar/217a4e11562a6db6c9b73c6825a051ce?s=80&d=mm&r=g&.jpg" alt="profile"></img>} 
                            id="profile-dropdown"
                            alignRight>
                            <NavDropdown.Item href="#profile">Your Profile</NavDropdown.Item>
                            <NavDropdown.Item href="#yourReservations">Your Reservations</NavDropdown.Item>
                            <NavDropdown.Item href="#favStores">Favorite Stores</NavDropdown.Item>
                            <NavDropdown.Divider />
                            <NavDropdown.Item href="#SignOut">Sign Out</NavDropdown.Item>
                        </NavDropdown>
                    </Nav>
                </Navigation.Collapse>
            </Navigation >
        )
    }
}
