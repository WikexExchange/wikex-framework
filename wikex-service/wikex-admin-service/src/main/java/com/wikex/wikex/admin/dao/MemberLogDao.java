package com.wikex.wikex.admin.dao;


import com.wikex.wikex.admin.entity.MemberLog;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface MemberLogDao extends MongoRepository<MemberLog,Long> {
}
