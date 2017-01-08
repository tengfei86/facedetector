package bit.facetracker.model;

import java.util.List;

/**
 * Created by blade on 08/01/2017.
 */

public class Result {
    public Attribute attributes;
    public String name;
    public CellImage cel_image;

    class Attribute {
        public int age;
        public int attractive;
        public List<Expresstion> expression;

        class Expresstion {
            public String kind;
            public Double probability;
        }
    }

    class CellImage {
        public String original;
        public String thumbnail;
    }


}
