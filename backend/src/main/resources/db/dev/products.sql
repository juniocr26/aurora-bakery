-- Fictional local demo products, not customer or administrator accounts.
INSERT INTO products(id,slug,name,description,price,category,featured,availability) VALUES
('11111111-1111-4111-8111-111111111111','sourdough','Country sourdough','Slow fermented, with a golden crust.',24.90,'Bread',true,'AVAILABLE'),
('22222222-2222-4222-8222-222222222222','cinnamon-roll','Cinnamon roll','Soft dough, cinnamon and vanilla glaze.',12.50,'Pastry',false,'AVAILABLE'),
('33333333-3333-4333-8333-333333333333','strawberry-cake','Strawberry cake','Fresh strawberries and delicate cream.',89.00,'Cake',true,'TEMPORARILY_UNAVAILABLE')
ON CONFLICT DO NOTHING;
