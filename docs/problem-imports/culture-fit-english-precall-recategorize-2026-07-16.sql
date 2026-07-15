INSERT IGNORE INTO category(path) VALUES
('culture_fit:about_me'),
('culture_fit:about_me:self_introduction'),
('culture_fit:about_me:personality'),
('culture_fit:about_me:learning_new_technologies'),
('culture_fit:motivation'),
('culture_fit:motivation:company_understanding'),
('culture_fit:motivation:why_company'),
('culture_fit:resume'),
('culture_fit:resume:resume_walkthrough'),
('culture_fit:resume:project_impact'),
('culture_fit:resume:current_role'),
('culture_fit:relocation'),
('culture_fit:relocation:dubai_relocation');

UPDATE problem
SET category_id = (SELECT id FROM category WHERE path = 'culture_fit:about_me:self_introduction')
WHERE content = 'Please introduce yourself. / Please tell me about yourself.';

UPDATE problem
SET category_id = (SELECT id FROM category WHERE path = 'culture_fit:about_me:personality')
WHERE content = 'Tell us about your personality.';

UPDATE problem
SET category_id = (SELECT id FROM category WHERE path = 'culture_fit:about_me:learning_new_technologies')
WHERE content = 'How do you learn new technologies?';

UPDATE problem
SET category_id = (SELECT id FROM category WHERE path = 'culture_fit:motivation:company_understanding')
WHERE content = 'Do you know anything about our company?';

UPDATE problem
SET category_id = (SELECT id FROM category WHERE path = 'culture_fit:motivation:why_company')
WHERE content = 'Why are you applying for this company? / Why talabat? / Why are you interested in this role?';

UPDATE problem
SET category_id = (SELECT id FROM category WHERE path = 'culture_fit:relocation:dubai_relocation')
WHERE content = 'Would you be willing to relocate to Dubai to work?';
