class CreateComments < ActiveRecord::Migration
  def self.up
    create_table :comments do |t|
      t.string :name
      t.string :email
      t.text :text, :null => false
      t.references :commentable, :polymorphic => true
      t.timestamps :null => false
    end
    
    add_index :comments, [:commentable_type, :commentable_id, :created_at], :name => 'by_created_at'
  end

  def self.down
    drop_table :comments
  end
end
