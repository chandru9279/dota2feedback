class CreateVotes < ActiveRecord::Migration
  def self.up
    create_table :votes do |t|
      t.string :ip_address, :null => false
      t.integer :option, :null => false
      t.references :voteable, :null => false, :polymorphic => true
      t.timestamps :null => false
    end
    
    add_index :votes, [:voteable_type, :voteable_id]
    add_index :votes, [:ip_address, :voteable_type, :voteable_id]
  end

  def self.down
    drop_table :votes
  end
end
