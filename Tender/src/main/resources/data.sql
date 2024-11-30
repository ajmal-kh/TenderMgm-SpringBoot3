insert into role(rolename) values('BIDDER'),('APPROVER');

insert into users(username,company_name,email,password,role_id) values('a','c1','a@gmail.com','$2a$12$LrcbpDLiHMt5JwCngWFcL.w98WstF9XPibc.kV6nxzNtyEVQGsGai',1),
('b','c1','b@gmail.com','$2a$12$LrcbpDLiHMt5JwCngWFcL.w98WstF9XPibc.kV6nxzNtyEVQGsGai',1),
('c','c1','c@gmail.com','$2a$12$LrcbpDLiHMt5JwCngWFcL.w98WstF9XPibc.kV6nxzNtyEVQGsGai',2);