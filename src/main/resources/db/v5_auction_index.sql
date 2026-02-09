CREATE INDEX idx_auction_property_id_is_deleted
    ON auctions (property_id, is_deleted);

CREATE INDEX idx_auction_property_status
    ON auctions (property_id, is_deleted, status);
