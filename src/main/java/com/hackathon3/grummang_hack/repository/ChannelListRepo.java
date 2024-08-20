package com.hackathon3.grummang_hack.repository;

import com.hackathon3.grummang_hack.model.entity.ChannelList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChannelListRepo extends JpaRepository<ChannelList, Long> {


    Optional<ChannelList> findByChannelId(String channelId);

    @Query("SELECT cl.orgSaas.id FROM ChannelList cl WHERE cl.channelId = :channelId")
    int findOrgSaaSIdByChannelId(@Param("channelId")String teamId);

    boolean existsByChannelId(String channelId);
}
