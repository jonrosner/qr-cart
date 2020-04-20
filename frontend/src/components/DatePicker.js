import React, {useState} from 'react'
import { Row, Col } from 'react-bootstrap'

export default function DatePicker({ currentDay, onDayPick }) {
    const [day, setDay] = useState(currentDay);

    const changeday = (i) => {
        onDayPick(day + i)
        setDay(day + i)
    }

    return (
            <Row>
                <Col><button  onClick={() => changeday(-1)}>-1</button></Col>
                <Col>{day}</Col>
                <Col><button onClick={() => changeday(+1)}>+1</button></Col>
            </Row>
    )
}