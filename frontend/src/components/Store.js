import React, { Component } from 'react'
// import Container from 'react-bootstrap/Container'
// import Row from 'react-bootstrap/Container'
// import Table from 'react-bootstrap/Table';
import SideBar from './SideBar'
import SideBarItem from './SideBarItem'
// import Button from 'react-bootstrap/Button'
// import DatePicker from './DatePicker'
// import moment from 'moment';
// import ReactTimeslotCalendar from 'react-timeslot-calendar';
import Reservations from './Reservation'

import './styles/stores.css'
import 'react-datetime/css/react-datetime.css'
import QrCode from './QrCodeModal';

export default class Store extends Component {
    dateRef = React.createRef();

    constructor(props) {
        super(props);
        this.state = {
            storeId: this.props.match.params.id,
            store: null,
            todaysReservations: null
        };
    }

    componentDidMount() {
        fetch(`http://andromeda.goma-cms.org:1337/api/store/${this.state.storeId}`).then(async res => {
            const data = await res.json();
            this.setState({
                ...this.state,
                store: data,
            })
        }).catch(error => {
            console.log("AN ERROR", error)
        })
    }

    render() {
        const now = new Date();
        const currentDay = Math.floor(now/8.64e7);
        
        const onDayPick = (day) => {
            fetch(`http://andromeda.goma-cms.org:1337/api/store/${this.state.storeId}/reservations/${day}`).then(async res => {
            const data = await res.json();
            this.setState({
                ...this.state,
                reservations: data,
            })
        }).catch(error => {
            console.log("AN ERROR", error)
        })
        }

        return (
            <div className="d-flex" id="wrapper">
                <SideBar>
                    <SideBarItem link="#" text="All Reservation Slots" />
                </SideBar>

                <Reservations selectedStore={this.state.storeId} todaysReservations={this.state.todaysReservations}></Reservations>
            </div>
        )
    }
}
