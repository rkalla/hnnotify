package models;

import java.io.Serializable;

import com.mongodb.DBObject;

public interface IModel extends Serializable {
	public DBObject toDBObject();
}