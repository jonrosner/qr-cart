import React from 'react'
import QRCode from 'react-qr-code';


export default function QrCode({ reservationId }) {
    return (
        <div>
            <p>Your QR Code</p>
            <QRCode value={reservationId} />
        </div>
    )
}