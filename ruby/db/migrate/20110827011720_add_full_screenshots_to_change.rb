class AddFullScreenshotsToChange < ActiveRecord::Migration
  def self.up
    add_column :changes, :full_screenshot_file_name, :string
    add_column :changes, :full_screenshot_content_type, :string
    add_column :changes, :full_screenshot_file_size, :string
    add_column :changes, :full_screenshot_updated_at, :string
  end

  def self.down
    remove_column :changes, :full_screenshot_file_name
    remove_column :changes, :full_screenshot_content_type
    remove_column :changes, :full_screenshot_file_size
    remove_column :changes, :full_screenshot_updated_at
  end
end
