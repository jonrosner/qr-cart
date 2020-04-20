import React from 'react'
import "./styles/popup.css"
import { Card, Button } from 'react-bootstrap'

function StorePopup({ id, name, pplInStore, address, zip, city }) {
    return (
        <div className="popup">
            <Card style={{ border: 'none' }}>
            <Card.Img variant="top" src="rsz_rewe.jpg" />
            <Card.Body>
                <Card.Title>{name}</Card.Title>
                <Card.Text>
                    {address}, {zip} {city}
                </Card.Text>
                <Card.Text>
                    Öffnungszeiten: 08:00 - 20:00
                </Card.Text>
                <Card.Text>
                    Momentane Auslastung: {pplInStore} / 100
                </Card.Text>
                <Button variant="success" href={`/stores/${id}`}>Reservation</Button>
            </Card.Body>
            </Card>
        </div>
    )
}

export default StorePopup;