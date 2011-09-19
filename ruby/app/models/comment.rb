class Comment < ActiveRecord::Base
  include Gravtastic
  gravtastic
  
  belongs_to :commentable, :polymorphic => true, :counter_cache => true
  
  validates :text, :presence => true
  validates :commentable, :presence => true
  validates_format_of :email, :with => /\A([^@\s]+)@((?:[-a-z0-9]+\.)+[a-z]{2,})\Z/i, :on => :create, :message => 'must be a valid e-mail', :allow_blank => true
  
  def score
    likes - dislikes
  end
  
  def approval
    likes + dislikes == 0 ? 0 : ((likes.to_f / (likes.to_f + dislikes.to_f)) * 100)
  end
end
