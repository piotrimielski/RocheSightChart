package com.givevision.rochesightchart.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

/**
 * @author Piotr
 */
@Dao
public interface AcuityDao {
    @Insert
    void insertAcuity(Acuity acuity);

    @Query("SELECT * FROM acuities ORDER BY id")
    List<Acuity> getAllAcuities();

    @Query("SELECT * FROM acuities WHERE id IN (:acuityIds)")
    List<Acuity> loadAcuitiesByIds(int[] acuityIds);

    @Query("SELECT * FROM acuities WHERE in_server = :inDataBase")
    List<Acuity> loadAcuitiesByInDatabase(boolean inDataBase);

    @Query("SELECT * FROM acuities WHERE created_at LIKE :createdAt LIMIT 1")
    Acuity findAcuityByCreatedDate(Date createdAt);

    @Query("SELECT * FROM acuities WHERE modified_at LIKE :modifiedAt LIMIT 1")
    Acuity findAcuityByModifiedDate(Date modifiedAt);

    @Query("SELECT * FROM acuities WHERE id LIKE :id LIMIT 1")
    Acuity findAcuityById(int id);

    @Query("SELECT * FROM acuities WHERE user_id =:userId")
    List<Acuity> getAcuitiesByUserId(int userId);

    @Update
    void updateAcuity(Acuity acuity);

    @Delete
    void deleteAcuity(Acuity acuity);

    @Query("DELETE FROM acuities")
    void deleteAcuityTable();

    @Query("SELECT * FROM acuities WHERE user_id =:userId AND in_server=0 ORDER BY id DESC LIMIT 1")
    Acuity getLastAcuityByUserId(int userId);

    @Query("UPDATE acuities SET in_server=1 WHERE id=:appID;")
    void upDateInServerByAppId(int appID);
}
