create table orders (
    order_id uuid not null,
    created_at timestamp(6) with time zone,
    customer_id uuid,
    status varchar(255),
    total_amount numeric(38,2),
    updated_at timestamp(6) with time zone,
    constraint orders_pkey primary key (order_id),
    constraint orders_status_check check (status in ('PENDING_PAYMENT', 'PAID', 'PAYMENT_FAILED'))
);

create table order_items (
    order_id uuid not null,
    product_id varchar(255),
    product_name varchar(255),
    quantity integer,
    unit_price numeric(38,2),
    constraint fk_order_items_order foreign key (order_id) references orders (order_id)
);

create table order_outbox (
    id uuid not null,
    aggregate_id uuid,
    attempts integer not null,
    created_at timestamp(6) with time zone,
    event_type varchar(255),
    last_error text,
    message_key varchar(255),
    next_attempt_at timestamp(6) with time zone,
    payload text not null,
    published_at timestamp(6) with time zone,
    status varchar(255),
    topic varchar(255),
    updated_at timestamp(6) with time zone,
    constraint order_outbox_pkey primary key (id),
    constraint order_outbox_status_check check (status in ('PENDING', 'PUBLISHED'))
);

create index idx_orders_customer_id on orders (customer_id);
create index idx_order_outbox_status_next_attempt on order_outbox (status, next_attempt_at);
