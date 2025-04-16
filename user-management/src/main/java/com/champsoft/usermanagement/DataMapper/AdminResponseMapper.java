package com.champsoft.usermanagement.DataMapper;


import com.champsoft.usermanagement.DataAccess.Admin;
import com.champsoft.usermanagement.DataAccess.AdminId;
import com.champsoft.usermanagement.Presentation.AdminResponseModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {AdminId.class, UUID.class})
public interface AdminResponseMapper {
    @Mapping(target = "adminId", expression = "java(admin.getAdminId().getUuid())")
    @Mapping(target = "username", expression = "java(admin.getUsername())")
    AdminResponseModel adminToAdminResponseModel(Admin admin);
    List<AdminResponseModel> adminToAdminResponseModel(List<Admin> admins);
}