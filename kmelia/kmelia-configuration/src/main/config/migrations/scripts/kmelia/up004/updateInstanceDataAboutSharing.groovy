sql.executeUpdate("update st_instance_data set value='0' where name='useFileSharing' and value='no'")
sql.executeUpdate("update st_instance_data set value='1' where name='useFileSharing' and value='yes'")
sql.executeUpdate("update st_instance_data set value='0' where name='usePublicationSharing' and value='no'")
sql.executeUpdate("update st_instance_data set value='1' where name='usePublicationSharing' and value='yes'")
sql.executeUpdate("update st_instance_data set value='0' where name='useFolderSharing' and value='no'")
sql.executeUpdate("update st_instance_data set value='1' where name='useFolderSharing' and value='yes'")