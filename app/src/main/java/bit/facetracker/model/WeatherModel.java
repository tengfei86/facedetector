package bit.facetracker.model;

import java.util.List;

/**
 * Created by blade on 05/04/2017.
 */

public class WeatherModel {

    public Coord coord;
    public List<Weather> weather;
    public Main main;
    public Wind wind;
    public Cloud clouds;
    public Sys sys;

    public class Coord {
        public float lon;
        public float lat;
    }

    public class Weather {
        public int id;
        public String main;
        public String description;
        public String icon;
    }

    public class Main {
        public float temp;
        public float pressure;
        public float humidity;
        public float temp_min;
        public float temp_max;
    }

    public class Wind {
        public float speed;
        public float deg;
    }

    public class Cloud {
        public float all;
    }

    public class Sys {
        public int type;
        public int id;
        public float message;
        public String country;
        public long sunrise;
        public long sunset;
    }



}
