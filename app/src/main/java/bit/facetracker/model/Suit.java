package bit.facetracker.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by blade on 30/03/2017.
 */

public class Suit implements Serializable {

    // main image
    public String url = "";

    public List<Item> items;

    public class Item implements Serializable{
        public String url = "";
    }

}
