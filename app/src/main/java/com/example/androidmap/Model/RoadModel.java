package com.example.androidmap.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.mapbox.geojson.Point;

public class RoadModel implements Parcelable {
    private String name;
    private Point coordinate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Point getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Point coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeSerializable(this.coordinate);
    }

    public RoadModel() {
    }

    protected RoadModel(Parcel in) {
        this.name = in.readString();
        this.coordinate = (Point) in.readSerializable();
    }

    public static final Creator<RoadModel> CREATOR = new Creator<RoadModel>() {
        @Override
        public RoadModel createFromParcel(Parcel source) {
            return new RoadModel(source);
        }

        @Override
        public RoadModel[] newArray(int size) {
            return new RoadModel[size];
        }
    };
}
