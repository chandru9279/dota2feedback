class AddLikesToComment < ActiveRecord::Migration
  def self.up
    add_column :comments, :likes, :integer, :null => false, :default => 0
    add_column :comments, :dislikes, :integer, :null => false, :default => 0
  end

  def self.down
    remove_column :comments, :likes
    remove_column :comments, :dislikes
  end
end
