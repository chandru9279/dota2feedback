class Change < ActiveRecord::Base
  has_many :votes, :as => 'voteable', :dependent => :delete_all
  has_many :comments, :as => 'commentable', :dependent => :delete_all
  
  validates :name, :presence => true, :uniqueness => true
  validates :category, :presence => true
  
  has_attached_file :icon, :styles => { :original => '205x115>' }, :default_url => "/missing/:style/:attachment.png"
  has_attached_file :face, :styles => { :original => '120x115!' }, :default_url => "/missing/:style/:attachment.png"
  has_attached_file :screenshot, :styles => { :original => '120x115!' }, :default_url => "/missing/:style/:attachment.png"
  has_attached_file :full_screenshot, :default_url => "/missing/:style/:attachment.png"
  
  scope :liked, order('COALESCE((likes / (likes + dislikes)), 0) DESC, (likes - dislikes) DESC, name ASC')
  scope :hated, order('COALESCE((likes / (likes + dislikes)), 0) ASC, (likes - dislikes) ASC, name DESC')
  
  PER_PAGE = 5
  TYPES = ['Hero', 'Item', 'Skill', 'Miscellaneous']
  
  SECTIONS = {
    'Hero' => [:icon, :face, :screenshot],
    'Item' => [:icon],
    'Skill' => [:icon, :screenshot],
    'Miscellaneous' => [:screenshot]
  }
  
  def score
    likes - dislikes
  end
  
  def approval
    likes + dislikes == 0 ? 0 : ((likes.to_f / (likes.to_f + dislikes.to_f)) * 100)
  end
end
