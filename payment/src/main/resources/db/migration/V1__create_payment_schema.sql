create table payments (
    payment_id uuid not null,
    amount numeric(38,2),
    created_at timestamp(6) with time zone,
    customer_id uuid,
    order_id uuid,
    reason varchar(255),
    status varchar(255),
    updated_at timestamp(6) with time zone,
    constraint payments_pkey primary key (payment_id),
    constraint payments_status_check check (status in ('PROCESSING', 'APPROVED', 'DECLINED', 'FAILED'))
);

create table payment_outbox (
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
    constraint payment_outbox_pkey primary key (id),
    constraint payment_outbox_status_check check (status in ('PENDING', 'PUBLISHED'))
);

create index idx_payments_order_id on payments (order_id);
create index idx_payment_outbox_status_next_attempt on payment_outbox (status, next_attempt_at);
