class CreateChanges < ActiveRecord::Migration
  def self.up
    create_table :changes do |t|
      t.string :name, :null => false
      t.string :category, :null => false
      t.integer :likes, :null => false, :default => 0
      t.integer :dislikes, :null => false, :default => 0
      t.integer :comments_count, :null => false, :default => 0
      
      t.string :icon_file_name
			t.string :icon_content_type
			t.integer :icon_file_size
			t.datetime :icon_updated_at
      
      t.string :face_file_name
			t.string :face_content_type
			t.integer :face_file_size
			t.datetime :face_updated_at
      
      t.string :screenshot_file_name
			t.string :screenshot_content_type
			t.integer :screenshot_file_size
			t.datetime :screenshot_updated_at
      
      t.timestamps :null => false
    end
    
    add_index :changes, [:category, :name]
    add_index :changes, [:category, :likes, :name]
    add_index :changes, [:category, :dislikes, :name]
    add_index :changes, [:category, :comments_count]
  end

  def self.down
    drop_table :changes
  end
end
