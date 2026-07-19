create table addresses (
    address_id varchar(36) not null,
    city varchar(255),
    complement varchar(255),
    created_at timestamp(6) without time zone,
    number varchar(255),
    state varchar(255),
    street varchar(255),
    updated_at timestamp(6) without time zone,
    zip_code varchar(255),
    constraint addresses_pkey primary key (address_id)
);

create table users (
    user_id varchar(36) not null,
    created_at timestamp(6) without time zone not null,
    email varchar(255) not null,
    login varchar(255) not null,
    name varchar(255) not null,
    password varchar(255) not null,
    updated_at timestamp(6) without time zone not null,
    user_type smallint not null,
    address_id varchar(36),
    constraint users_pkey primary key (user_id),
    constraint uk_users_email unique (email),
    constraint uk_users_login unique (login),
    constraint uk_users_address_id unique (address_id),
    constraint users_user_type_check check (user_type >= 0 and user_type <= 1),
    constraint fk_users_address foreign key (address_id) references addresses (address_id)
);

create table user_roles (
    user_id varchar(36) not null,
    role varchar(255) not null,
    constraint user_roles_pkey primary key (user_id, role),
    constraint user_roles_role_check check (role in ('ADMIN', 'USER', 'OWNER')),
    constraint fk_user_roles_user foreign key (user_id) references users (user_id)
);
