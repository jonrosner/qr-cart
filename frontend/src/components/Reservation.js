import React, { Component } from 'react'
// import Container from 'react-bootstrap/Container'
import Row from 'react-bootstrap/Container'
import Table from 'react-bootstrap/Table'
// import SideBar from './SideBar'
// import SideBarItem from './SideBarItem'
import Button from 'react-bootstrap/Button'
import Modal from 'react-bootstrap/Modal'
import Form from 'react-bootstrap/Form'
import QRCode from 'react-qr-code';

import './styles/stores.css'
import 'react-datetime/css/react-datetime.css'

function ReservationModal(props) {
    const [duration, setDuration] = React.useState(15);
    const [reservationId, setReservationId] = React.useState(null);

    const sendReservation = async () => {
        
        let mm = props.reservationTime.getMinutes().toString()
        if (mm.length == 1) {
            mm = "0" + mm
        }

        const startDate = `2020-04-19T${props.reservationTime.getHours()}:${mm}:00.000+0200`

        const body = JSON.stringify({
            startDate: startDate,
            duration: duration,
            customerId: "9015d7ab-172f-4861-b1e2-20c2ec87ae47",
            storeId: props.storeId
        })
        const result = await fetch('//qrcart.goma-cms.org/api/reservation', {
            method: 'POST',
            body: body
        })
        const resId = await result.text()
        setReservationId(resId)
    }

    return (
        <Modal
            {...props}
            size="lg"
            aria-labelledby="contained-modal-title-vcenter"
            centered
        >
            <Modal.Header closeButton>
                <Modal.Title id="contained-modal-title-vcenter">
                    Create Reservation
          </Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <h4>Reservation</h4>
                {
                    reservationId ?
                    <div>
                        <p>Your Reservation Code</p>
                        <QRCode value={reservationId} />
                    </div> :
                    <Form controlId="myform">
                        <Form.Group controlId="myform.start">
                            <Form.Label>Start Date</Form.Label>
                            <Form.Control disabled type="text" placeholder={props.reservationTime}/>
                        </Form.Group>
                        <Form.Group controlId="myform.duration" onChange={(e) => setDuration(e.target.value)}>
                            <Form.Label>Duration</Form.Label>
                            <Form.Control as="select">
                            <option value={15}>15 minutes</option>
                            <option value={30}>30 minutes</option>
                            <option value={45}>45 minutes</option>
                            </Form.Control>
                        </Form.Group>
                        <Button variant="primary" onClick={() => sendReservation()}>
                            Submit
                        </Button>
                    </Form>

                }
            </Modal.Body>
            <Modal.Footer>
                <Button onClick={() => {
                    props.onHide()
                    setReservationId(null)
                    setDuration(15)
                }}>Close</Button>
            </Modal.Footer>
        </Modal>
    );
}

function Reservation(props) {
    const [modalShow, setModalShow] = React.useState(false);

    return (
        <>
            <Button 
            style={{cursor: props.props[1]}}
            variant={props.props[2]}
            {...props.props[3]}
            onClick={() => {
                setModalShow(true)
            }}
            >{props.props[0]}</Button>

            <ReservationModal
                reservationTime={props.props[4]}
                storeId={props.props[5]}
                show={modalShow}
                onHide={() => setModalShow(false)}
            />
        </>
    )
}

function fillTimes(props) {
    var startDate = new Date();
    var endDate = new Date()
    console.log(startDate);

    startDate.setHours(10);
    startDate.setMinutes(0);
    startDate.setSeconds(0);

    var startTime = startDate.getTime();

    endDate.setHours(20);
    endDate.setMinutes(0);
    endDate.setSeconds(0);

    var endTime = endDate.getTime();

    var currentTime = startTime;
    props.push(startDate)
    while (currentTime < endTime) {
        currentTime += 15 * 60 * 1000;
        props.push(currentTime);
    }
    return props;
}
export default class Reservations extends Component {
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
            store: {},
            storeID: props.selectedStore,
            todaysReservations: props.todaysReservations,
            times: []
        };
    }

    componentDidMount() {
        var t = fillTimes(this.state.times);
        fetch(`http://andromeda.goma-cms.org:1337/api/store/${this.state.storeID}`).then(async res => {
            const data = await res.json();
            this.setState({
                ...this.state,
                store: data,
                times: t,
            })
        }).catch(error => {
            console.log("AN ERROR", error)
        })
    }

    renderTable() {
        var keyID = 0;

        return this.state.times.map((time, index) => {
            var date = new Date(time);
            keyID++;
            // const { id, name, city, currentNoPeopleInStore, maximumCapacity } = this.state.stores.reduce(id===this.state.storeID) //destructuring
            const currentCustomers = Math.floor(Math.random() * Math.floor(this.state.store.maximumCapacity));
            const capacitiy = (currentCustomers * 100) / this.state.store.maximumCapacity;

            var buttonStyle = 'success'
            var opts = {}
            var description = 'Create Reservation';
            var cursor = 'pointer';
            if (capacitiy === 100) {
                buttonStyle = 'light';
                opts['disabled'] = 'disabled';
                opts['active'] = 'active';
                description = 'Store full';
                cursor = 'default';
            } else if (capacitiy > 80) {
                buttonStyle = 'danger';
            } else if (capacitiy > 60) {
                buttonStyle = 'warning';
            }

            return (
                <tr key={keyID}>
                    <td>{date.getDate() + '-' + (date.getMonth()<10?'0':''+1) + date.getMonth() + '-' + date.getFullYear()}</td>
                    <td>{date.getHours() + ':' + (date.getMinutes()<10?'0':'') + date.getMinutes()}</td>
                    <td>{currentCustomers}</td>
                    <td>{this.state.store.maximumCapacity}</td>
                    <td>
                        <Reservation props={[
                            description,
                            cursor,
                            buttonStyle,
                            opts,
                            date,
                            this.state.storeID
                            ]} />
                    </td>
                </tr>
            )
        })
    }

    render() {
        return (
            <Row id="active-reservations">
                <h1>{this.state.store.name}</h1>
                <Table striped borderless className="customTable shadow p-3 mb-5 bg-white rounded">
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Time</th>
                            <th>Current</th>
                            <th>Max Capacity</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {this.renderTable()}
                    </tbody>
                </Table>
            </Row>
        )
    }
}
