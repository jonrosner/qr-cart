import React, { Component } from 'react'
// import Container from 'react-bootstrap/Container'
import Row from 'react-bootstrap/Container'
import Table from 'react-bootstrap/Table';
import SideBar from './SideBar'
import SideBarItem from './SideBarItem'
import Button from 'react-bootstrap/Button'

import './styles/stores.css'
import 'react-datetime/css/react-datetime.css'

export default class Stores extends Component {
    dateRef = React.createRef();
    // {
    //     "id": "254bd894-d5ec-4c6a-aa6c-23df2bc6fb6d",
    //     "locationLat": 48.1381,
    //     "locationLng": 11.3731,
    //     "name": "AEZ Germering",
    //     "address": "Münchner Straße 1",
    //     "zip": "82110",
    //     "city": "Germering",
    //     "maximumCapacity": 10,
    //     "reservationPercentage": 0.3,
    //     "checkInTimeDuration": 10,
    //     "minSlotSize": 15,
    //     "maxSlotSize": 30,
    //     "errorMargin": 5,
    //     "currentNoPeopleInStore": 10,
    //     "employeeList": [
    //     "a6c196f6-1fe6-407d-a81b-194997a74e60"
    //     ],
    //     "reservationList": [],
    //     "numUpcomingReservations": 0,
    //     "numRemainingReservations": 0
    // }

    constructor(props) {
        super(props);
        this.state = {
            stores: [],
        };
    }

    componentDidMount() {
        fetch('http://andromeda.goma-cms.org:1337/api/store/all').then(async res => {
            const data = await res.json();
            this.setState({
                ...this.state,
                stores: data,
            })
        }).catch(error => {
            console.log("AN ERROR", error)
        })
    }

    renderTable() {
        return this.state.stores.map((store, index) => {
            const { id, name, city, address, currentNoPeopleInStore, maximumCapacity} = store //destructuring

            var buttonStyle = 'success'
            var description = 'Create Reservation';

            return (
                <tr key={id}>
                    <td>{name}</td>
                    <td>{city}</td>
                    <td>{address}</td>
                    <td>{currentNoPeopleInStore} of {maximumCapacity}</td>
                    <td><Button href={`/stores/${id}`} block variant={buttonStyle}>{description}</Button></td>
                </tr>
            )
        })
    }

    render() {
        return (
            <div className="d-flex" id="wrapper">
                <SideBar>
                    <SideBarItem link="#" text="All Stores" />
                </SideBar>

                <Row id="active-reservations">
                    <h1>Stores</h1>
                    <Table striped borderless className="customTable shadow p-3 mb-5 bg-white rounded">
                        <thead>
                            <tr>
                                <th>Date</th>
                                <th>Time</th>
                                <th>Address</th>
                                <th>Current Capacity</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {this.renderTable()}
                        </tbody>
                    </Table>
                </Row>
            </div>
        )
    }
}
