drop database if exists kxtest;
create database kxtest;
drop user if exists 'kxreport' @'%';
create user 'kxreport' @'%' identified by 'kxreport';
grant select,
  execute,
  create temporary tables on *.* to 'kxreport' @'%';
drop table if exists kxtest.demo;
create table kxtest.demo (
  id integer unsigned auto_increment primary key,
  name varchar(40)
);
insert into kxtest.demo (name)
values ('admin'),
  ('tom'),
  ('jerry');