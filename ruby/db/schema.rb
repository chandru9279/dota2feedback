# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended to check this file into your version control system.

ActiveRecord::Schema.define(:version => 20110910155829) do

  create_table "changes", :force => true do |t|
    t.string   "name",                                        :null => false
    t.string   "category",                                    :null => false
    t.integer  "likes",                        :default => 0, :null => false
    t.integer  "dislikes",                     :default => 0, :null => false
    t.integer  "comments_count",               :default => 0, :null => false
    t.string   "icon_file_name"
    t.string   "icon_content_type"
    t.integer  "icon_file_size"
    t.datetime "icon_updated_at"
    t.string   "face_file_name"
    t.string   "face_content_type"
    t.integer  "face_file_size"
    t.datetime "face_updated_at"
    t.string   "screenshot_file_name"
    t.string   "screenshot_content_type"
    t.integer  "screenshot_file_size"
    t.datetime "screenshot_updated_at"
    t.datetime "created_at",                                  :null => false
    t.datetime "updated_at",                                  :null => false
    t.string   "full_screenshot_file_name"
    t.string   "full_screenshot_content_type"
    t.string   "full_screenshot_file_size"
    t.string   "full_screenshot_updated_at"
    t.string   "full_screenshot_link"
  end

  add_index "changes", ["category", "comments_count"], :name => "index_changes_on_category_and_comments_count"
  add_index "changes", ["category", "dislikes", "name"], :name => "index_changes_on_category_and_dislikes"
  add_index "changes", ["category", "likes", "name"], :name => "index_changes_on_category_and_likes"
  add_index "changes", ["category", "name"], :name => "index_changes_on_category_and_name"

  create_table "comments", :force => true do |t|
    t.string   "name"
    t.string   "email"
    t.text     "text",                            :null => false
    t.integer  "commentable_id"
    t.string   "commentable_type"
    t.datetime "created_at",                      :null => false
    t.datetime "updated_at",                      :null => false
    t.integer  "likes",            :default => 0, :null => false
    t.integer  "dislikes",         :default => 0, :null => false
  end

  add_index "comments", ["commentable_type", "commentable_id", "created_at"], :name => "by_created_at"

  create_table "votes", :force => true do |t|
    t.string   "ip_address",    :null => false
    t.integer  "option",        :null => false
    t.integer  "voteable_id",   :null => false
    t.string   "voteable_type", :null => false
    t.datetime "created_at",    :null => false
    t.datetime "updated_at",    :null => false
  end

  add_index "votes", ["ip_address", "voteable_type", "voteable_id"], :name => "ip_address"
  add_index "votes", ["voteable_type", "voteable_id"], :name => "index_votes_on_voteable_type_and_voteable_id"

end
