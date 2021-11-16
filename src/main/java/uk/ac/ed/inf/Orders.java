package uk.ac.ed.inf;

import java.sql.Date;
import java.util.ArrayList;

public class Orders {
    String orderNo;
    String customer;
    String deliverTo;

    public Orders (String orderNo, String customer, String deliverTo){
        this.orderNo = orderNo;
        this.customer = customer;
        this.deliverTo = deliverTo;
    }

}
