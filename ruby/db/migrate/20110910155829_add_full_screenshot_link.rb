class AddFullScreenshotLink < ActiveRecord::Migration
  def self.up
    add_column :changes, :full_screenshot_link, :string
  end

  def self.down
    remove_column :changes, :full_screenshot_link
  end
end
