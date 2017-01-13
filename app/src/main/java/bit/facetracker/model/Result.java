package bit.facetracker.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by blade on 08/01/2017.
 */

public class Result implements Serializable{
    public Attribute attributes;
    public String name;
    public CellImage cel_image;
    public int face_num;

    public class Attribute implements Serializable {
        public int age;
        public int attractive;
        public List<Expresstion> expression;
        public List<Expresstion> gender;
        public class Expresstion implements Serializable {
            public String kind;
            public Double probability;
        }
    }

    public class CellImage implements Serializable {
        public String original;
        public String thumbnail;
    }
}
